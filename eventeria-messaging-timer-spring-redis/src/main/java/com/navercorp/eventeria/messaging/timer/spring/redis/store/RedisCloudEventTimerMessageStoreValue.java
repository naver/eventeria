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

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * Model of timer message to store redis.
 */
public class RedisCloudEventTimerMessageStoreValue {
	private final UUID id;
	private final byte[] message;
	private final Instant releaseDateTime;
	private final boolean cloudEventType;

	/**
	 * @param id unique id for each timer message distinguish.
	 * @param message serialized value of timer message.
	 * @param releaseDateTime scheduled time to release.
	 * @param cloudEventType whether serialized message is type of {@link io.cloudevents.CloudEvent}.
	 */
	@ConstructorProperties({"id", "message", "releaseDateTime", "cloudEventType"})
	public RedisCloudEventTimerMessageStoreValue(
		UUID id,
		byte[] message,
		Instant releaseDateTime,
		boolean cloudEventType
	) {
		this.id = id;
		this.message = message;
		this.releaseDateTime = releaseDateTime;
		this.cloudEventType = cloudEventType;
	}

	public UUID getId() {
		return this.id;
	}

	public byte[] getMessage() {
		return this.message;
	}

	public Instant getReleaseDateTime() {
		return this.releaseDateTime;
	}

	public boolean isCloudEventType() {
		return this.cloudEventType;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		RedisCloudEventTimerMessageStoreValue that = (RedisCloudEventTimerMessageStoreValue)obj;
		return Objects.equals(id, that.id)
			&& Arrays.equals(message, that.message)
			&& Objects.equals(releaseDateTime, that.releaseDateTime)
			&& Objects.equals(cloudEventType, that.cloudEventType);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(id);
		result = 31 * result + Objects.hashCode(releaseDateTime);
		result = 31 * result + Arrays.hashCode(message);
		result = 31 * result + Objects.hashCode(cloudEventType);
		return result;
	}

	@Override
	public String toString() {
		return "RedisCloudEventTimerMessageStoreValue{"
			+ "id=" + id
			+ ", message=" + Arrays.toString(message)
			+ ", releaseDateTime=" + releaseDateTime
			+ ", cloudEventType=" + cloudEventType
			+ '}';
	}
}
