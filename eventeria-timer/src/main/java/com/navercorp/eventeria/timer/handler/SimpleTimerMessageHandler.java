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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.eventeria.timer.contract.handler.TimerMessageHandler;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStore;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStoreValue;

public class SimpleTimerMessageHandler implements TimerMessageHandler {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final TimerMessageStore timerMessageStore;
	private final int countPerRelease;

	public SimpleTimerMessageHandler(
		TimerMessageStore timerMessageStore,
		int countPerRelease
	) {
		this.timerMessageStore = timerMessageStore;
		this.countPerRelease = countPerRelease;
	}

	@Override
	public boolean isTimerMessage(Object message) {
		return TimerMessageHandlerSupports.getReleaseDateTime(message).isPresent();
	}

	@Override
	public String register(Object message) {
		TimerMessageStoreValue storeValue = TimerMessageHandlerSupports.toTimerMessageStoreValue(message);
		this.timerMessageStore.save(storeValue, null);
		return storeValue.toString();
	}

	@Override
	public void releaseMessages(Consumer<Object> consumeReleasedMessage) {
		Instant scheduleTime = Instant.now();

		boolean schedule = true;
		while (schedule) {
			schedule = this.schedulePersistedMessages(scheduleTime, consumeReleasedMessage);
		}
	}

	@Override
	public long getDelayedMessageCount() {
		return this.timerMessageStore.count(Instant.now(), null);
	}

	@Override
	public void cancel(String registeredId) {
		this.timerMessageStore.remove(UUID.fromString(registeredId), null);
	}

	// return need rescheduling partitions
	private boolean schedulePersistedMessages(Instant scheduleTime, Consumer<Object> consumeReleaseMessage) {
		boolean needReschedule = false;

		List<TimerMessageStoreValue> releaseValues = this.timerMessageStore.findReleaseValues(
			scheduleTime, this.countPerRelease, null);

		if (releaseValues == null || releaseValues.isEmpty()) {
			return needReschedule;
		}

		// 추가 처리할 데이터가 더 있기 때문에 reschedule partition 으로 추가합니다.
		if (releaseValues.size() == this.countPerRelease) {
			needReschedule = true;
		}

		releaseValues.sort(Comparator.comparing(TimerMessageStoreValue::getReleaseDateTime));

		for (TimerMessageStoreValue releaseValue : releaseValues) {
			try {
				consumeReleaseMessage.accept(releaseValue.getMessage());
				this.timerMessageStore.remove(releaseValue.getId(), null);
			} catch (Throwable throwable) {
				logger.error("timer handler release message is failed. "
					+ "This message would be ignored and retry next scheduling. storeValue: "
					+ releaseValue);
			}
		}

		return needReschedule;
	}
}
