/*
 * Eventeria
 *
 * Copyright (c) 2022-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.eventeria.timer.handler;

import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.eventeria.messaging.contract.distribution.PartitionGenerator;
import com.navercorp.eventeria.timer.contract.handler.TimerMessageHandler;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStore;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStoreValue;

/**
 * A implementation of {@link TimerMessageHandler} that handles messages with distributed way.
 */
public class DistributedTimerMessageHandler implements TimerMessageHandler {
	private static final Logger LOG = LoggerFactory.getLogger(DistributedTimerMessageHandler.class);

	private final TimerMessageStore timerMessageStore;
	private final int countPerRelease;
	private final PartitionGenerator partitionGenerator;
	private final int registeredPartitionCount;
	private final int seekPartitionCount;

	@Nullable
	private final Executor executor;

	/**
	 * @param timerMessageStore
	 * @param countPerRelease
	 * @param partitionGenerator
	 * @param registeredPartitionCount partition count used on register time.
	 * @param seekPartitionCount partition count used on retrieve time.
	 *                           Normally has same value with registeredPartitionCount,
	 *                           but can be different when increasing partition for backward compatibility.
	 * @param executor
	 */
	public DistributedTimerMessageHandler(
		TimerMessageStore timerMessageStore,
		int countPerRelease,
		PartitionGenerator partitionGenerator,
		int registeredPartitionCount,
		int seekPartitionCount,
		@Nullable Executor executor
	) {
		this.timerMessageStore = timerMessageStore;
		this.countPerRelease = countPerRelease;
		this.partitionGenerator = partitionGenerator;
		this.registeredPartitionCount = registeredPartitionCount;
		this.seekPartitionCount = seekPartitionCount;
		this.executor = executor;
	}

	@Override
	public boolean isTimerMessage(Object message) {
		return TimerMessageHandlerSupports.getReleaseDateTime(message).isPresent();
	}

	@Override
	public String register(Object message) {
		TimerMessageStoreValue storeValue = TimerMessageHandlerSupports.toTimerMessageStoreValue(message);
		int partition = this.getPartition(message);
		this.timerMessageStore.save(storeValue, partition);
		return this.registerId(storeValue.getId(), partition);
	}

	@Override
	public void releaseMessages(Consumer<Object> consumeReleasedMessage) {
		Instant scheduleTime = Instant.now();

		List<Integer> schedulePartitions = new ArrayList<>();
		for (int i = 0; i < this.seekPartitionCount; i++) {
			schedulePartitions.add(i);
		}

		while (!schedulePartitions.isEmpty()) {
			schedulePartitions = this.schedulePersistedMessages(schedulePartitions, scheduleTime,
				consumeReleasedMessage);
		}
	}

	@Override
	public long getDelayedMessageCount() {
		Instant now = Instant.now();

		long count = 0;
		for (int i = 0; i < this.seekPartitionCount; i++) {
			long size = this.timerMessageStore.count(now, i);
			count += size;
		}
		return count;
	}

	@Override
	public void cancel(String registeredId) {
		Entry<UUID, Integer> storeValueIdPartition = this.splitRegisterId(registeredId);
		this.timerMessageStore.remove(storeValueIdPartition.getKey(), storeValueIdPartition.getValue());
	}

	// return need rescheduling partitions
	private List<Integer> schedulePersistedMessages(
		List<Integer> schedulePartitions, Instant scheduleTime, Consumer<Object> consumeReleaseMessage
	) {
		List<Integer> reschedulePartitions = new CopyOnWriteArrayList<>();
		List<CompletableFuture<Void>> futures = new ArrayList<>();
		for (int partition : schedulePartitions) {
			Runnable runnable = () -> {
				List<TimerMessageStoreValue> releaseValues = this.timerMessageStore.findReleaseValues(
					scheduleTime, this.countPerRelease, partition);

				if (releaseValues == null || releaseValues.isEmpty()) {
					return;
				}

				// add partition to reschedulePartition because there are remaining data to process
				if (releaseValues.size() == this.countPerRelease) {
					reschedulePartitions.add(partition);
				}

				releaseValues.sort(Comparator.comparing(TimerMessageStoreValue::getReleaseDateTime));

				int successCount = 0;
				for (TimerMessageStoreValue releaseValue : releaseValues) {
					try {
						consumeReleaseMessage.accept(releaseValue.getMessage());
						this.timerMessageStore.remove(releaseValue.getId(), partition);
						successCount++;
					} catch (Throwable throwable) {
						LOG.error(
							"timer handler release message is failed. "
								+ "This message would be ignored and retry next scheduling. storeValue: {}",
							releaseValue
						);
					}
				}

				// If there is no succeed message among release targets, do not reschedule.
				if (successCount == 0 && reschedulePartitions.contains(partition)) {
					reschedulePartitions.remove(partition);
				}
			};

			if (this.executor != null) {
				futures.add(CompletableFuture.runAsync(runnable, this.executor));
			} else {
				runnable.run();
			}
		}

		if (!futures.isEmpty()) {
			CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]))
				.join();
		}

		return reschedulePartitions;
	}

	private int getPartition(Object message) {
		return this.partitionGenerator.partition(message, this.registeredPartitionCount);
	}

	private String registerId(UUID storeValueId, int partition) {
		return storeValueId + ":{" + partition + "}";
	}

	private Entry<UUID, Integer> splitRegisterId(String registerId) {
		int idIndex = registerId.indexOf(":{");
		if (idIndex < 0) {
			throw new IllegalArgumentException(
				"registerId is not distributed timer message handler Id. registerId: " + registerId
			);
		}

		try {
			String storeValueId = registerId.substring(0, idIndex);
			String partition = registerId.substring(idIndex + 1, registerId.length() - 2);
			return new SimpleEntry<>(UUID.fromString(storeValueId), Integer.valueOf(partition));
		} catch (Exception ex) {
			throw new IllegalArgumentException(
				"registerId is not distributed timer message handler Id. registerId: " + registerId
			);
		}
	}
}
