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

package com.navercorp.eventeria.messaging.kafka.fixture;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

public class EventFixtures extends DomainContextBase {
	@Provide
	public Arbitrary<TestDomainEvent> testDomainEvent() {
		return Builders.withBuilder(TestDomainEvent::builder)
			.use(Arbitraries.randomValue(r -> UUID.randomUUID()).map(UUID::toString))
			.in(TestDomainEvent.TestDomainEventBuilder::sourceId)
			.use(Arbitraries.longs()
				.greaterOrEqual(1)
				.lessOrEqual(10000000)  // guard to overflow
				.injectNull(0.1))
			.in(TestDomainEvent.TestDomainEventBuilder::sourceVersion)
			.use(Arbitraries.randomValue(r -> Instant.now()))
			.in(TestDomainEvent.TestDomainEventBuilder::occurrenceTime)
			.use(Arbitraries.strings().alpha())
			.in(TestDomainEvent.TestDomainEventBuilder::name)
			.use(Arbitraries.randomValue(r -> UUID.randomUUID().toString()))
			.in(TestDomainEvent.TestDomainEventBuilder::correlationId)
			.use(Arbitraries.randomValue(r -> UUID.randomUUID().toString()))
			.in(TestDomainEvent.TestDomainEventBuilder::operationId)
			.use(Arbitraries.randomValue(r -> URI.create("http://" + UUID.randomUUID())))
			.in(TestDomainEvent.TestDomainEventBuilder::dataSchema)
			.use(Arbitraries.strings().alpha())
			.in(TestDomainEvent.TestDomainEventBuilder::subject)
			.use(Arbitraries.strings().alpha())
			.in(TestDomainEvent.TestDomainEventBuilder::partitionKey)
			.use(Arbitraries.maps(
				Arbitraries.strings().alpha().ofMinLength(1),
				Arbitraries.strings().alpha().ofMinLength(1).map(Object.class::cast)
			).ofMinSize(1))
			.in(TestDomainEvent.TestDomainEventBuilder::extensions)
			.build(TestDomainEvent.TestDomainEventBuilder::build);
	}
}
