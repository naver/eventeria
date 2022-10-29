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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import lombok.Builder;
import lombok.Getter;

import com.navercorp.eventeria.domain.annotation.DomainEventHandler;
import com.navercorp.eventeria.domain.arbitrary.ArbitraryUtils;
import com.navercorp.eventeria.domain.entity.AnnotatedMessageHandlerAggregateRoot;
import com.navercorp.eventeria.messaging.contract.event.AbstractDomainEvent;
import com.navercorp.eventeria.messaging.contract.event.Event;

public class AbstractAggregateRootFixtures extends DomainContextBase {

	@Provide
	public Arbitrary<TestAggregateRoot> testAggregateRoot() {
		return Builders.withBuilder(TestAggregateRoot::builder)
			.use(ArbitraryUtils.aggregateRootId()).in(TestAggregateRoot.TestAggregateRootBuilder::id)
			.use(ArbitraryUtils.aggregateRootVersion()).in(TestAggregateRoot.TestAggregateRootBuilder::version)
			.use(Arbitraries.integers()).in(TestAggregateRoot.TestAggregateRootBuilder::amount)
			.build(TestAggregateRoot.TestAggregateRootBuilder::build);
	}

	@Getter
	public static class TestAggregateRoot extends AnnotatedMessageHandlerAggregateRoot {
		private String id;
		private Long version;
		private int amount;
		private transient List<String> handledDomainEventIds = new ArrayList<>();

		@Builder
		public TestAggregateRoot(String id, Long version, int amount) {
			this.id = id;
			this.version = version;
			this.amount = amount;
		}

		public TestAggregateRoot(TestAggregateRootCreated createdEvent) {
			this.raiseEvent(createdEvent);
		}

		public void raiseEvent(Event event) {
			super.raiseEvent(event);
		}

		@DomainEventHandler
		private void onTestDomainEvent(TestDomainEvent testDomainEvent) {
			this.handledDomainEventIds.add(testDomainEvent.getId());
		}

		@DomainEventHandler
		private void onTestAggregateRootCreated(TestAggregateRootCreated event) {
			this.handledDomainEventIds.add(event.getId());
			this.id = event.getSourceId();
			this.version = event.getSourceVersion();
			this.amount = event.getAmount();
		}
	}

	@Getter
	public static class TestAggregateRootCreated extends AbstractDomainEvent {
		private int amount;

		@Builder
		TestAggregateRootCreated(String id, Long version, Instant occurrenceTime, int amount) {

			super(id, version, occurrenceTime);
			this.amount = amount;
		}

		@Override
		public String getSourceType() {
			return TestAggregateRoot.class.getName();
		}
	}
}
