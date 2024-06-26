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

package com.navercorp.eventeria.messaging.contract.event;

import java.time.Instant;
import java.time.OffsetDateTime;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;

/**
 * a abstract class to support implementation of {@link DomainEvent}
 */
public abstract class AbstractDomainEvent extends AbstractEvent implements DomainEvent {
	protected AbstractDomainEvent() {
		super();
	}

	protected AbstractDomainEvent(String sourceId) {
		super(sourceId);
	}

	protected AbstractDomainEvent(String sourceId, @Nullable Long version) {
		this(sourceId, version, Instant.now());
	}

	protected AbstractDomainEvent(String sourceId, @Nullable Long version, Instant occurrenceTime) {
		super(sourceId, version, occurrenceTime);
	}

	protected AbstractDomainEvent(String sourceId, @Nullable Long version, OffsetDateTime occurrenceTime) {
		super(sourceId, version, occurrenceTime);
	}

	@Nullable
	protected Long determineRaisedVersion(EventRaisableSource source) {
		Long version = source.getVersion();
		if (version != null) {
			version++;
		}
		return version;
	}

	// kotlin nullable 회피를 위한 재정의
	@Nonnull
	@Override
	public String getSourceId() {
		return super.getSourceId();
	}
}
