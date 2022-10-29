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

package com.navercorp.eventeria.domain.entity;

import static com.navercorp.eventeria.domain.util.TestAssertions.assertCloseNow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.domains.Domain;

import com.navercorp.eventeria.domain.entity.AnnotatedAggregateMetaModelTest.SimpleCommandSpec;
import com.navercorp.eventeria.domain.entity.AnnotatedAggregateMetaModelTest.SimpleDomainEventSpec;
import com.navercorp.eventeria.domain.fixture.AbstractAggregateRootFixtures;
import com.navercorp.eventeria.domain.fixture.AbstractAggregateRootFixtures.TestAggregateRoot;
import com.navercorp.eventeria.domain.fixture.AbstractAggregateRootFixtures.TestAggregateRootCreated;
import com.navercorp.eventeria.domain.fixture.TestDomainEvent;
import com.navercorp.eventeria.domain.fixture.TestEvent;
import com.navercorp.eventeria.domain.fixture.TestIntegrationEvent;
import com.navercorp.eventeria.domain.fixture.TestSimpleEvent;
import com.navercorp.eventeria.messaging.contract.channel.MessageHandler;
import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.DomainEvent;
import com.navercorp.eventeria.messaging.contract.event.Event;
import com.navercorp.eventeria.messaging.contract.event.IntegrationEvent;
import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;
import com.navercorp.eventeria.messaging.contract.source.RaiseEventHandler;

class AnnotatedMessageHandlerAggregateRootTest {
	@Example
	void isAssignableFrom() {
		assertThat(AggregateRoot.class).isAssignableFrom(AnnotatedMessageHandlerAggregateRoot.class);
		assertThat(MessageHandler.class).isAssignableFrom(AnnotatedMessageHandlerAggregateRoot.class);
		assertThat(EventRaisableSource.class).isAssignableFrom(AnnotatedMessageHandlerAggregateRoot.class);
	}

	@Example
	@Domain(AbstractAggregateRootFixtures.class)
	@Label(
		"RaiseEventHandler 를 구현한 event 가 raise 되면, "
		+ "event 의 sourceId, sourceVersion, occurrenceTime 이 셋팅되고, pending 된다."
	)
	void raiseAggregateRaiseEvent(@ForAll TestAggregateRoot sut) {
		// given
		TestEvent event = new TestEvent("name");

		// when
		sut.raiseEvent(event);

		// then
		assertThat(RaiseEventHandler.class).isAssignableFrom(event.getClass());
		assertThat(event.getSourceId()).isEqualTo(sut.getId());
		if (sut.getVersion() == null) {
			assertThat(event.getSourceVersion()).isNull();
		} else {
			assertThat(event.getSourceVersion()).isEqualTo(sut.getVersion() + 1);
		}
		assertCloseNow(event.getOccurrenceTime());
		assertThat(sut.events()).hasSize(1);
	}

	@Example
	@Domain(AbstractAggregateRootFixtures.class)
	@Label("raise 된 event 가 RaiseEventHandler 를 구현하지 않았으면, pending 만 한다.")
	void raiseSimpleEvent(@ForAll TestAggregateRoot sut) {
		// given
		Event event = new TestSimpleEvent("name");

		// when
		sut.raiseEvent(event);

		// then
		assertThat(RaiseEventHandler.class.isAssignableFrom(event.getClass())).isFalse();
		assertThat(event.getSourceId()).isNull();
		assertThat(event.getSourceVersion()).isNull();
		assertThat(event.getOccurrenceTime()).isNull();
		assertThat(sut.events()).hasSize(1);
	}

	@Example
	@Domain(AbstractAggregateRootFixtures.class)
	@Label("AbstractAggregateRoot 에서 null Event 가 발생하면, NullPointerException 이 발생한다.")
	void raiseNullEvent(@ForAll TestAggregateRoot sut) {
		assertThatThrownBy(() -> sut.raiseEvent(null))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessageContaining("event");
	}

	@Example
	@Domain(AbstractAggregateRootFixtures.class)
	@Label("raise 된 event 의 sourceType 이 다르면, IllegalArgumentException 이 발샌한다.")
	void raiseDifferentSourceTypeEvent(@ForAll TestAggregateRoot sut) {
		Event event = new TestEvent("name") {
			@Override
			public String getSourceType() {
				return "different-source-type";
			}
		};

		assertThatThrownBy(() -> sut.raiseEvent(event))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(sut.getClass().getName())
			.hasMessageContaining(event.getSourceType());
	}

	@Example
	void raiseEventForNewAggregateRoot() {
		// given
		TestAggregateRootCreated event = TestAggregateRootCreated.builder()
			.id(UUID.randomUUID().toString())
			.version(1L)
			.amount(10)
			.occurrenceTime(Instant.now())
			.build();

		// when
		TestAggregateRoot sut = new TestAggregateRoot(event);

		// then
		List<String> handledDomainEventIds = sut.getHandledDomainEventIds();
		assertThat(handledDomainEventIds).hasSize(1);
		assertThat(handledDomainEventIds.get(0)).isEqualTo(event.getId());

		Iterable<Event> pendingEvents = sut.events();
		assertThat(pendingEvents.iterator().next()).isSameAs(event);

		assertThat(event.getSourceId()).isEqualTo(sut.getId());
		if (sut.getVersion() != null) {
			assertThat(event.getSourceVersion()).isEqualTo(sut.getVersion());
		}
		assertThat(event.getSourceType()).isEqualTo(sut.getClass().getName());
	}

	@Example
	@Domain(AbstractAggregateRootFixtures.class)
	void raiseDomainEvent(@ForAll TestAggregateRoot sut) {
		// given
		DomainEvent domainEvent = new TestDomainEvent();

		// when
		sut.raiseEvent(domainEvent);

		// then
		List<String> handledDomainEventIds = sut.getHandledDomainEventIds();
		assertThat(handledDomainEventIds).hasSize(1);
		assertThat(handledDomainEventIds.get(0)).isEqualTo(domainEvent.getId());

		Iterable<Event> pendingEvents = sut.events();
		assertThat(pendingEvents.iterator().next()).isSameAs(domainEvent);

		assertThat(domainEvent.getSourceId()).isEqualTo(sut.getId());
		if (sut.getVersion() != null) {
			assertThat(domainEvent.getSourceVersion()).isEqualTo(sut.getVersion() + 1);
		}
		assertThat(domainEvent.getSourceType()).isEqualTo(sut.getClass().getName());
	}

	@Example
	@Domain(AbstractAggregateRootFixtures.class)
	void raiseIntegrationEvent(@ForAll TestAggregateRoot sut) {
		// given
		IntegrationEvent integrationEvent = new TestIntegrationEvent();

		// when
		sut.raiseEvent(integrationEvent);

		// then
		Iterable<Event> pendingEvents = sut.events();
		assertThat(pendingEvents.iterator().next()).isSameAs(integrationEvent);

		assertThat(integrationEvent.getSourceId()).isEqualTo(sut.getId());
		assertThat(integrationEvent.getSourceVersion()).isEqualTo(sut.getVersion());
		assertThat(integrationEvent.getSourceType()).isEqualTo(sut.getClass().getName());
	}

	@Example
	@Domain(AbstractAggregateRootFixtures.class)
	@Label("events 는 발생한 Event 를 순서대로 반환한다.")
	void events(@ForAll TestAggregateRoot sut) {
		// given
		List<Event> events = Arrays.asList(
			new TestEvent("name"),
			new TestEvent("name2"));

		events.forEach(sut::raiseEvent);

		// when
		Iterable<Event> actual = sut.events();

		// then
		assertThat(actual).hasSameElementsAs(events);
		assertThat(sut.events()).isNotEmpty();
	}

	@Example
	@Domain(AbstractAggregateRootFixtures.class)
	@Label("clearEvents 는 pending 된 이벤트를 제거한다.")
	void clearEvents(@ForAll TestAggregateRoot sut) {
		// given
		List<Event> events = Arrays.asList(
			new TestEvent("name"),
			new TestEvent("name2"));

		events.forEach(sut::raiseEvent);

		// when
		sut.clearEvents();

		// then
		assertThat(sut.events()).isEmpty();
	}

	@Example
	void handleCommandIsRequiredCommandHandlerTrueThrowsException(@ForAll @StringLength(min = 1) String id) {
		AnnotatedMessageHandlerAggregateRoot sut = new RequiredCommandHandlerAggregateAnnotated(id);
		Command command = new SimpleCommandSpec();
		assertThatThrownBy(() -> sut.handleCommand(command))
			.isExactlyInstanceOf(RequiredCommandHandlerException.class)
			.hasMessageContaining("HandlerMethod not found for Command.")
			.hasMessageContaining(command.getClass().getName());
	}

	@Example
	void handleDomainEventIsRequiredDomainEventHandlerTrueThrowsException(@ForAll @StringLength(min = 1) String id) {
		AnnotatedMessageHandlerAggregateRoot sut = new RequiredDomainEventHandlerAggregateAnnotated(id);
		DomainEvent domainEvent = new SimpleDomainEventSpec();
		assertThatThrownBy(() -> sut.handleDomainEvent(domainEvent))
			.isExactlyInstanceOf(RequiredDomainEventHandlerException.class)
			.hasMessageContaining("HandlerMethod not found for domainEvent.")
			.hasMessageContaining(domainEvent.getClass().getName());
	}

	static class NoMetaModelAggregate implements AggregateRoot {
		private String id;

		public NoMetaModelAggregate(String id) {
			this.id = id;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@Override
		public Iterable<Event> events() {
			return null;
		}

		@Override
		public void clearEvents() {

		}

		@Override
		public String getId() {
			return this.id;
		}
	}

	static class RequiredCommandHandlerAggregateAnnotated extends AnnotatedMessageHandlerAggregateRoot {
		private String id;

		public RequiredCommandHandlerAggregateAnnotated(String id) {
			this.id = id;
		}

		@Override
		public boolean isRequiredCommandHandler() {
			return true;
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Nullable
		@Override
		public Long getVersion() {
			return null;
		}
	}

	static class RequiredDomainEventHandlerAggregateAnnotated extends AnnotatedMessageHandlerAggregateRoot {
		private String id;

		public RequiredDomainEventHandlerAggregateAnnotated(String id) {
			this.id = id;
		}

		@Override
		public boolean isRequiredDomainEventHandler() {
			return true;
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Nullable
		@Override
		public Long getVersion() {
			return null;
		}
	}
}
