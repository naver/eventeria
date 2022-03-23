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

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import com.navercorp.eventeria.domain.arbitrary.ArbitraryUtils;
import com.navercorp.eventeria.messaging.contract.event.DomainEvent;

public class DomainEventFixtures extends DomainContextBase {

	@Provide
	public Arbitrary<DomainEvent> testDomainEvent() {
		return Builders.withBuilder(TestDomainEvent::builder)
			.use(ArbitraryUtils.aggregateRootId()).in(TestDomainEvent.TestDomainEventBuilder::sourceId)
			.use(ArbitraryUtils.aggregateRootVersion()).in(TestDomainEvent.TestDomainEventBuilder::sourceVersion)
			.use(ArbitraryUtils.currentTime()).in(TestDomainEvent.TestDomainEventBuilder::occurrenceTime)
			.use(Arbitraries.strings()).in(TestDomainEvent.TestDomainEventBuilder::name)
			.build(TestDomainEvent.TestDomainEventBuilder::build);
	}
}
