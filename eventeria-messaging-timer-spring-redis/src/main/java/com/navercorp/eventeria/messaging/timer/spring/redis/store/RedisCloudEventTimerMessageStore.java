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

package com.navercorp.eventeria.messaging.timer.spring.redis.store;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventMessageReaderWriter;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStore;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStoreValue;

/**
 * An implementation of {@link TimerMessageStore} for integration with redis using spring-data-redis
 */
public class RedisCloudEventTimerMessageStore implements TimerMessageStore {
	private static final Logger LOG = LoggerFactory.getLogger(RedisCloudEventTimerMessageStore.class);

	private static final long EXPIRATION_BUFFER_HOURS_VALUE = 24L;

	private final String redisKeyPrefix;
	private final RedisOperations<String, String> redisIndexOperations;
	private final RedisOperations<String, RedisCloudEventTimerMessageStoreValue> redisValueOperations;
	private final CloudEventMessageReaderWriter cloudEventMessageReaderWriter;

	/**
	 * @param redisKeyPrefix prefix to classify timer message.
	 * @param redisIndexOperations redis operations to index actual values.
	 * @param redisValueOperations redis operations to handle timer messages.
	 * @param cloudEventMessageReaderWriter (de)serializer for persisting timer messages.
	 */
	public RedisCloudEventTimerMessageStore(
		String redisKeyPrefix,
		RedisOperations<String, String> redisIndexOperations,
		RedisOperations<String, RedisCloudEventTimerMessageStoreValue> redisValueOperations,
		CloudEventMessageReaderWriter cloudEventMessageReaderWriter
	) {
		this.redisKeyPrefix = redisKeyPrefix;
		this.redisIndexOperations = redisIndexOperations;
		this.redisValueOperations = redisValueOperations;
		this.cloudEventMessageReaderWriter = cloudEventMessageReaderWriter;
	}

	/**
	 * Save message with adding index.
	 *
	 * @param storeValue
	 * @param partition
	 */
	@Override
	public void save(
		TimerMessageStoreValue storeValue,
		@Nullable Integer partition
	) {
		String indexKey = this.generateIndexKey(partition);
		this.redisIndexOperations.opsForZSet()
			.add(indexKey, storeValue.getId().toString(), (double)storeValue.getReleaseDateTime().getEpochSecond());

		String valueKey = this.generateKey(storeValue.getId(), partition);
		RedisCloudEventTimerMessageStoreValue redisStoreValue = this.toRedisStoreValue(storeValue);
		long expirationMillis =
			storeValue.getReleaseDateTime().plus(EXPIRATION_BUFFER_HOURS_VALUE, ChronoUnit.HOURS).toEpochMilli()
				- Instant.now().toEpochMilli();
		this.redisValueOperations.opsForValue()
			.set(valueKey, redisStoreValue, expirationMillis, TimeUnit.MILLISECONDS);
	}

	/**
	 * Remove message with deleting index.
	 *
	 * @param storeValueId
	 * @param partition
	 */
	@Override
	public void remove(UUID storeValueId, @Nullable Integer partition) {
		String indexKey = this.generateIndexKey(partition);
		this.redisIndexOperations.opsForZSet().remove(indexKey, storeValueId.toString());

		try {
			String valueKey = this.generateKey(storeValueId, partition);
			this.redisValueOperations.delete(valueKey);
		} catch (Exception ex) {
			LOG.warn("Remove timer message value has error. But it would be ignore. storeValueId: {}, partition: {}",
				storeValueId, partition, ex);
		}
	}

	/**
	 * Count number of stored messages in index operation.
	 *
	 * @param conditionDateTime time to search before
	 * @param partition
	 * @return number of rows exists in index of partition and has time before conditionDateTime.
	 */
	@Override
	public long count(Instant conditionDateTime, @Nullable Integer partition) {
		String indexKey = this.generateIndexKey(partition);

		double conditionScore = (double)conditionDateTime.getEpochSecond();

		Long result = this.redisIndexOperations.opsForZSet().count(indexKey, 0, conditionScore);

		return result != null ? result : 0L;
	}

	/**
	 * find all messages by a scheduled time and partitions.
	 *
	 * @param conditionDateTime
	 * @param count
	 * @param partition
	 * @return
	 */
	@Override
	public List<TimerMessageStoreValue> findReleaseValues(
		Instant conditionDateTime,
		int count,
		@Nullable Integer partition
	) {
		String indexKey = this.generateIndexKey(partition);
		double conditionScore = (double)conditionDateTime.getEpochSecond();
		Set<TypedTuple<String>> indexedStoreValueIds = this.redisIndexOperations.opsForZSet()
			.rangeByScoreWithScores(indexKey, 0, conditionScore, 0, count);

		if (indexedStoreValueIds == null || indexedStoreValueIds.isEmpty()) {
			return Collections.emptyList();
		}

		List<TimerMessageStoreValue> result = new ArrayList<>();
		for (TypedTuple<String> tuple : indexedStoreValueIds) {
			String storeValueId = tuple.getValue();
			String valueKey = this.generateKey(UUID.fromString(storeValueId), partition);
			try {
				RedisCloudEventTimerMessageStoreValue redisStoreValue =
					this.redisValueOperations.opsForValue().get(valueKey);
				if (redisStoreValue == null) {
					this.redisIndexOperations.opsForZSet().remove(indexKey, storeValueId);
				} else {
					result.add(this.toTimerMessageStoreValue(redisStoreValue));
				}
			} catch (Throwable throwable) {
				LOG.error("timer handler persisted value can not be deserialize to cloudEvent. "
					+ "This message would be ignored and deleted from store. "
					+ "indexKey: {}, storeValueId: {}, score: {}", indexKey, storeValueId, tuple.getScore(), throwable);

				this.redisIndexOperations.opsForZSet().remove(indexKey, storeValueId);
				this.redisValueOperations.delete(valueKey);
			}
		}

		return result;
	}

	private String generateIndexKey(@Nullable Integer partition) {
		if (partition == null) {
			return this.redisKeyPrefix + ":timer:index";
		}

		return String.format("%s:timer:{%d}:index", this.redisKeyPrefix, partition);
	}

	private String generateKey(UUID storeValueId, @Nullable Integer partition) {
		if (partition == null) {
			return String.format("%s:timer:value:%s", this.redisKeyPrefix, storeValueId.toString());
		}

		return String.format("%s:timer:{%d}:value:%s", this.redisKeyPrefix, partition, storeValueId.toString());
	}

	private RedisCloudEventTimerMessageStoreValue toRedisStoreValue(TimerMessageStoreValue storeValue) {
		Object message = storeValue.getMessage();

		CloudEvent cloudEvent;
		boolean cloudEventType = false;
		if (message instanceof CloudEvent ce) {
			cloudEvent = ce;
			cloudEventType = true;
		} else if (message instanceof Message msg) {
			cloudEvent = this.cloudEventMessageReaderWriter.convert(msg);
		} else {
			throw new UnsupportedOperationException("unsupported");
		}

		byte[] serialized = this.cloudEventMessageReaderWriter.serialize(cloudEvent);
		return new RedisCloudEventTimerMessageStoreValue(
			storeValue.getId(),
			serialized,
			storeValue.getReleaseDateTime(),
			cloudEventType
		);
	}

	private TimerMessageStoreValue toTimerMessageStoreValue(RedisCloudEventTimerMessageStoreValue storeValue) {
		Object message;
		byte[] serialized = storeValue.getMessage();
		if (storeValue.isCloudEventType()) {
			message = this.cloudEventMessageReaderWriter.deserialize(serialized);
		} else {
			message = this.cloudEventMessageReaderWriter.read(serialized);
		}
		return new TimerMessageStoreValue(
			storeValue.getId(),
			message,
			storeValue.getReleaseDateTime()
		);
	}
}
