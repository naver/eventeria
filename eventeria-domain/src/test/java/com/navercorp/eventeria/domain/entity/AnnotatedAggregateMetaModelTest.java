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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.jqwik.api.Example;
import net.jqwik.api.Label;

import com.navercorp.eventeria.domain.annotation.CommandHandler;
import com.navercorp.eventeria.domain.annotation.DomainEventHandler;
import com.navercorp.eventeria.messaging.contract.command.AbstractCommand;
import com.navercorp.eventeria.messaging.contract.event.AbstractDomainEvent;
import com.navercorp.eventeria.messaging.contract.event.AbstractIntegrationEvent;

class AnnotatedAggregateMetaModelTest {
	@Example
	void newAggregateMetaModel() {
		// Given
		Class<SimpleAggregateSpec> aggregateType = SimpleAggregateSpec.class;

		// When
		AnnotatedAggregateMetaModel<SimpleAggregateSpec> actual = AnnotatedAggregateMetaModel.newAggregateMetaModel(
			aggregateType);

		// Then
		assertThat(actual).isNotNull();
	}

	@Example
	@Label("Argument 가 정의되지 않은 메소드에 @CommandHandler 가 정의되어 있으면 InitializeCommandHandlerException 이 발생합니다.")
	void newAggregateMetaModelNoArgsCommandHandlerThrowsException() {
		// Given
		Class<NoArgsCommandHandlerAggregateSpec> aggregateType
			= NoArgsCommandHandlerAggregateSpec.class;

		// When, Then
		assertThrows(InitializeCommandHandlerException.class, () ->
			AnnotatedAggregateMetaModel.newAggregateMetaModel(aggregateType));
	}

	@Example
	@Label("Argument 가 정의되지 않은 메소드에 @DomainEventHandler 가 정의되어 있으면 InitializeEventHandlerException 이 발생합니다.")
	void newAggregateMetaModelNoArgsDomainEventHandlerThrowsException() {
		// Given
		Class<NoArgsDomainEventHandlerAggregateSpec> aggregateType
			= NoArgsDomainEventHandlerAggregateSpec.class;

		// When, Then
		assertThrows(InitializeEventHandlerException.class, () ->
			AnnotatedAggregateMetaModel.newAggregateMetaModel(aggregateType));
	}

	@Example
	@Label("@CommandHandler 의 파라미터가 Command 타입이 아니면, InitializeCommandHandlerException 이 발생합니다.")
	void newAggregateMetaModelIncompatibleCommandHandlerThrowsException() {
		// Given
		Class<IncompatibleCommandHandlerAggregateSpec> aggregateType
			= IncompatibleCommandHandlerAggregateSpec.class;

		// When, Then
		assertThrows(InitializeCommandHandlerException.class, () ->
			AnnotatedAggregateMetaModel.newAggregateMetaModel(aggregateType));
	}

	@Example
	@Label("@DomainEventHandler 의 파라미터가 DomainEvent 타입이 아니면, InitializeEventHandlerException 이 발생합니다.")
	void newAggregateMetaModelIncompatibleDomainEventHandlerThrowsException() {
		// Given
		Class<IncompatibleDomainEventHandlerAggregateSpec> aggregateType
			= IncompatibleDomainEventHandlerAggregateSpec.class;

		// When, Then
		assertThrows(InitializeEventHandlerException.class, () ->
			AnnotatedAggregateMetaModel.newAggregateMetaModel(aggregateType));
	}

	@Example
	@Label("같은 DomainEvent 를 처리하는 @DomainEventHandler 가 중복되면 InitializeEventHandlerException 이 발생합니다.")
	void newAggregateMetaModelDuplicatedDomainEventHandlerThrowsException() {
		// Given
		Class<DuplicatedDomainEventHandlerAggregateSpec> aggregateType
			= DuplicatedDomainEventHandlerAggregateSpec.class;

		// When, Then
		assertThrows(InitializeEventHandlerException.class, () ->
			AnnotatedAggregateMetaModel.newAggregateMetaModel(aggregateType));
	}

	@Example
	@Label("같은 DomainEvent 를 처리하는 @CommandHandler 가 중복되면 InitializeCommandHandlerException 이 발생합니다.")
	void newAggregateMetaModelDuplicatedDomainCommandEventHandlerThrowsException() {
		// Given
		Class<DuplicatedDomainCommandEventHandlerAggregateSpec> aggregateType
			= DuplicatedDomainCommandEventHandlerAggregateSpec.class;

		// When, Then
		assertThrows(InitializeCommandHandlerException.class, () ->
			AnnotatedAggregateMetaModel.newAggregateMetaModel(aggregateType));
	}

	@Example
	@Label("@CommandHandler 를 Override 할 수 있습니다.")
	void newAggregateMetaModelOverrideCommandHandler() {
		// Given
		Class<OverrideHandlerAggregateSpec> aggregateType = OverrideHandlerAggregateSpec.class;

		// When
		AnnotatedAggregateMetaModel<OverrideHandlerAggregateSpec> actual
			= AnnotatedAggregateMetaModel.newAggregateMetaModel(aggregateType);

		// Then
		assertThat(actual).isNotNull();
	}

	@Example
	@Label("@DomainEventHandler 를 Override 할 수 있습니다.")
	void newAggregateMetaModelOverrideEventHandler() {
		// Given
		Class<OverrideHandlerAggregateSpec> aggregateType = OverrideHandlerAggregateSpec.class;

		// When
		AnnotatedAggregateMetaModel<OverrideHandlerAggregateSpec> actual
			= AnnotatedAggregateMetaModel.newAggregateMetaModel(aggregateType);

		// Then
		assertThat(actual).isNotNull();
	}

	static class SimpleAggregateSpec extends AnnotatedMessageHandlerAggregateRoot {
		@Override
		public String getId() {
			return null;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@DomainEventHandler
		void onDomainEvent(SimpleDomainEventSpec domainEvent) {
		}
	}

	static class NoArgsCommandHandlerAggregateSpec extends AnnotatedMessageHandlerAggregateRoot {
		@Override
		public String getId() {
			return null;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@CommandHandler
		private void noArgsCommandHandler() {
		}
	}

	static class NoArgsDomainEventHandlerAggregateSpec extends AnnotatedMessageHandlerAggregateRoot {
		@Override
		public String getId() {
			return null;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@DomainEventHandler
		private void noArgsDomainEventHandler() {
		}
	}

	static class IncompatibleCommandHandlerAggregateSpec extends AnnotatedMessageHandlerAggregateRoot {
		@Override
		public String getId() {
			return null;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@CommandHandler
		private void onDomainEvent(SimpleDomainEventSpec domainEvent) {
		}
	}

	static class IncompatibleDomainEventHandlerAggregateSpec extends AnnotatedMessageHandlerAggregateRoot {
		@Override
		public String getId() {
			return null;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@DomainEventHandler
		private void onIntegrationEvent(SimpleIntegrationEventSpec integrationEvent) {
		}
	}

	static class DuplicatedDomainCommandEventHandlerAggregateSpec extends AnnotatedMessageHandlerAggregateRoot {
		@Override
		public String getId() {
			return null;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@CommandHandler
		private void onCommand(SimpleCommandSpec command) {
		}

		@CommandHandler
		private void onCommand2(SimpleCommandSpec command) {
		}
	}

	static class DuplicatedDomainEventHandlerAggregateSpec extends AnnotatedMessageHandlerAggregateRoot {
		@Override
		public String getId() {
			return null;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@DomainEventHandler
		private void onDomainEvent(SimpleDomainEventSpec domainEvent) {
		}

		@DomainEventHandler
		private void onDomainEvent2(SimpleDomainEventSpec domainEvent) {
		}
	}

	static class OverrideHandlerAggregateSpec extends SimpleAggregateSpec {
		@CommandHandler
		private void onDomainCommand(SimpleCommandSpec command) {
		}

		@DomainEventHandler
		@Override
		void onDomainEvent(SimpleDomainEventSpec domainEvent) {
		}
	}

	static class SimpleCommandSpec extends AbstractCommand {
		@Override
		public String getSourceType() {
			return null;
		}
	}

	static class SimpleDomainEventSpec extends AbstractDomainEvent {
		@Override
		public String getSourceType() {
			return null;
		}
	}

	static class SimpleIntegrationEventSpec extends AbstractIntegrationEvent {
		@Override
		public String getSourceType() {
			return null;
		}
	}
}
