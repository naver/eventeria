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

package com.navercorp.eventeria.domain.fixture;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

import com.navercorp.eventeria.domain.fixture.AbstractAggregateRootFixtures.TestAggregateRoot;
import com.navercorp.eventeria.messaging.contract.event.Event;

@Getter
public class TestSimpleEvent implements Event {
	private UUID id;
	private String sourceId;
	private Long sourceVersion;
	private String name;
	private OffsetDateTime occurrenceTime;

	public TestSimpleEvent(String name) {
		this.name = name;
	}

	@Builder
	public TestSimpleEvent(String sourceId, Long sourceVersion, OffsetDateTime occurrenceTime, String name) {
		this.id = UUID.randomUUID();
		this.sourceId = sourceId;
		this.sourceVersion = sourceVersion;
		this.name = name;
		this.occurrenceTime = occurrenceTime;
	}

	@Override
	public String getSourceType() {
		return TestAggregateRoot.class.getName();
	}

	@Override
	public Optional<UUID> getCorrelationId() {
		return Optional.empty();
	}

	@Override
	public Optional<String> getOperationId() {
		return Optional.empty();
	}
}
