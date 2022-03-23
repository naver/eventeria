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

import lombok.Builder;

import com.navercorp.eventeria.domain.fixture.AbstractAggregateRootFixtures.TestAggregateRoot;
import com.navercorp.eventeria.messaging.contract.event.AbstractIntegrationEvent;

public class TestIntegrationEvent extends AbstractIntegrationEvent {
	private String name;

	public TestIntegrationEvent() {
	}

	public TestIntegrationEvent(String name) {
		this.name = name;
	}

	@Builder
	public TestIntegrationEvent(String sourceId, Long sourceVersion, OffsetDateTime occurrenceTime, String name) {
		super(sourceId, sourceVersion, occurrenceTime);
		this.name = name;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String getSourceType() {
		return TestAggregateRoot.class.getName();
	}
}
