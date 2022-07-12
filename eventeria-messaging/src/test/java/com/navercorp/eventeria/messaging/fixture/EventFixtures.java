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

package com.navercorp.eventeria.messaging.fixture;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import com.navercorp.eventeria.messaging.arbitrary.ArbitraryUtils;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent.TestDomainEventBuilder;
import com.navercorp.eventeria.messaging.fixture.TestEvent.TestEventBuilder;
import com.navercorp.eventeria.messaging.fixture.TestIntegrationEvent.TestIntegrationEventBuilder;

public class EventFixtures extends DomainContextBase {
	@Provide
	public Arbitrary<TestEvent> testEvent() {
		return Builders.withBuilder(TestEvent::builder)
			.use(ArbitraryUtils.sourceId()).in(TestEventBuilder::sourceId)
			.use(ArbitraryUtils.sourceVersion()).in(TestEventBuilder::sourceVersion)
			.use(ArbitraryUtils.currentTime()).in(TestEventBuilder::occurrenceTime)
			.use(Arbitraries.strings().alpha()).in(TestEventBuilder::name)
			.use(Arbitraries.maps(
				Arbitraries.strings().alpha().ofMinLength(1).filter(it -> !it.equals("id")),
				Arbitraries.strings().alpha().ofMinLength(1).map(Object.class::cast)
			).ofMinSize(1)).in(TestEventBuilder::extensions)
			.build(TestEventBuilder::build);
	}

	@Provide
	public Arbitrary<TestDomainEvent> testDomainEvent() {
		return Builders.withBuilder(TestDomainEvent::builder)
			.use(ArbitraryUtils.sourceId()).in(TestDomainEventBuilder::sourceId)
			.use(ArbitraryUtils.sourceVersion()).in(TestDomainEventBuilder::sourceVersion)
			.use(ArbitraryUtils.currentTime()).in(TestDomainEventBuilder::occurrenceTime)
			.use(Arbitraries.strings().alpha()).in(TestDomainEventBuilder::name)
			.use(Arbitraries.maps(
				Arbitraries.strings().alpha().ofMinLength(1).filter(it -> !it.equals("id")),
				Arbitraries.strings().alpha().ofMinLength(1).map(Object.class::cast)
			).ofMinSize(1)).in(TestDomainEventBuilder::extensions)
			.build(TestDomainEventBuilder::build);
	}

	@Provide
	public Arbitrary<TestIntegrationEvent> testIntegrationEvent() {
		return Builders.withBuilder(TestIntegrationEvent::builder)
			.use(ArbitraryUtils.sourceId()).in(TestIntegrationEventBuilder::sourceId)
			.use(ArbitraryUtils.sourceVersion()).in(TestIntegrationEventBuilder::sourceVersion)
			.use(ArbitraryUtils.currentTime()).in(TestIntegrationEventBuilder::occurrenceTime)
			.use(Arbitraries.strings().alpha()).in(TestIntegrationEventBuilder::name)
			.use(Arbitraries.maps(
				Arbitraries.strings().alpha().ofMinLength(1).filter(it -> !it.equals("id")),
				Arbitraries.strings().alpha().ofMinLength(1).map(Object.class::cast)
			).ofMinSize(1)).in(TestIntegrationEventBuilder::extensions)
			.build(TestIntegrationEventBuilder::build);
	}
}
