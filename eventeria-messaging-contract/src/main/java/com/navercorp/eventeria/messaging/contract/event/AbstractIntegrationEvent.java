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

import javax.annotation.Nullable;

import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;

/**
 * a abstract class to support implementation of {@link IntegrationEvent}
 */
public abstract class AbstractIntegrationEvent extends AbstractEvent implements IntegrationEvent {
	protected AbstractIntegrationEvent() {
		super(null);
	}

	protected AbstractIntegrationEvent(String sourceId) {
		super(sourceId);
	}

	protected AbstractIntegrationEvent(String sourceId, @Nullable Long version, Instant occurrenceTime) {
		super(sourceId, version, occurrenceTime);
	}

	protected AbstractIntegrationEvent(String sourceId, @Nullable Long version, OffsetDateTime occurrenceTime) {
		super(sourceId, version, occurrenceTime);
	}
}
