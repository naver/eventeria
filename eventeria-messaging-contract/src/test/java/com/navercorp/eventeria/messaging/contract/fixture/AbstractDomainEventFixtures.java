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

package com.navercorp.eventeria.messaging.contract.fixture;

import java.time.OffsetDateTime;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import lombok.Builder;

import com.navercorp.eventeria.messaging.contract.arbitrary.ArbitraryUtils;
import com.navercorp.eventeria.messaging.contract.event.AbstractDomainEvent;
import com.navercorp.eventeria.messaging.contract.fixture.AbstractDomainEventFixtures.TestDomainEvent.TestDomainEventBuilder;

public class AbstractDomainEventFixtures extends DomainContextBase {

	@Provide
	public Arbitrary<TestDomainEvent> testDomainEvent() {
		return Builders.withBuilder(TestDomainEvent::builder)
			.use(ArbitraryUtils.sourceId()).in(TestDomainEventBuilder::sourceId)
			.use(ArbitraryUtils.sourceVersion()).in(TestDomainEventBuilder::sourceVersion)
			.use(ArbitraryUtils.currentTime()).in(TestDomainEventBuilder::occurrenceTime)
			.use(Arbitraries.strings()).in(TestDomainEventBuilder::name)
			.build(TestDomainEventBuilder::build);
	}

	public static class TestDomainEvent extends AbstractDomainEvent {
		private String name;

		public TestDomainEvent() {
		}

		public TestDomainEvent(String name) {
			this.name = name;
		}

		@Builder
		public TestDomainEvent(String sourceId, Long sourceVersion, OffsetDateTime occurrenceTime, String name) {
			super(sourceId, sourceVersion, occurrenceTime);
			this.name = name;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public String getSourceType() {
			return TestEventRaisableSource.class.getName();
		}
	}
}
