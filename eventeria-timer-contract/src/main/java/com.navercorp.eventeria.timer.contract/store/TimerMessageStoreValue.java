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

package com.navercorp.eventeria.timer.contract.store;

import java.beans.ConstructorProperties;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * A value object contains metadata of timer message and persisted by {@link TimerMessageStore}
 */
public final class TimerMessageStoreValue {
	private final UUID id;    // unique id for each timer message distinguish
	private final Object message;
	private final Instant releaseDateTime;

	public TimerMessageStoreValue(Object message, Instant releaseDateTime) {
		this(UUID.randomUUID(), message, releaseDateTime);
	}

	@ConstructorProperties({"id", "message", "releaseDateTime"})
	public TimerMessageStoreValue(UUID id, Object message, Instant releaseDateTime) {
		this.id = id;
		this.message = message;
		this.releaseDateTime = releaseDateTime;
	}

	public UUID getId() {
		return this.id;
	}

	public Object getMessage() {
		return this.message;
	}

	public Instant getReleaseDateTime() {
		return this.releaseDateTime;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}

		TimerMessageStoreValue that = (TimerMessageStoreValue)obj;
		return Objects.equals(id, that.id)
			&& Objects.equals(message, that.message)
			&& Objects.equals(releaseDateTime, that.releaseDateTime);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, message, releaseDateTime);
	}

	@Override
	public String toString() {
		return "TimerMessageStoreValue{"
			+ "id=" + id
			+ ", message=" + message
			+ ", releaseDateTime=" + releaseDateTime
			+ '}';
	}
}
