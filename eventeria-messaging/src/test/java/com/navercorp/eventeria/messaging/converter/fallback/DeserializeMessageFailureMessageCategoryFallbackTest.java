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

package com.navercorp.eventeria.messaging.converter.fallback;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.temporal.ChronoUnit;

import org.assertj.core.data.TemporalUnitWithinOffset;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeTry;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.SimpleMessage;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.contract.command.SimpleCommand;
import com.navercorp.eventeria.messaging.contract.event.SimpleEvent;
import com.navercorp.eventeria.messaging.converter.CloudEventTypeAliasExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.CompositeCloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.MessageCategoryExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.PartitionKeyExtensionsConverter;
import com.navercorp.eventeria.messaging.fixture.CommandFixtures;
import com.navercorp.eventeria.messaging.fixture.CommandFixtures.TestCommand;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.MessageFixtures;
import com.navercorp.eventeria.messaging.fixture.MessageFixtures.TestPartitioned;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class DeserializeMessageFailureMessageCategoryFallbackTest {
	private final JacksonMessageSerializer messageSerializer = new JacksonMessageSerializer();
	private MessageToCloudEventConverter messageToCloudEventConverter;
	private DeserializeMessageFailureMessageCategoryFallback sut;

	@BeforeTry
	void setUp() {
		CloudEventMessageTypeAliasMapper typeAliasMapper = new CloudEventMessageTypeAliasMapper();
		typeAliasMapper.addCompatibleTypeAlias(TestDomainEvent.class, "deserializedType");

		this.messageToCloudEventConverter = new DefaultMessageToCloudEventConverter(
			new DefaultCloudEventAttributesConverter(),
			new CompositeCloudEventExtensionsConverter(
				new CloudEventTypeAliasExtensionsConverter(typeAliasMapper),
				new MessageCategoryExtensionsConverter(),
				new PartitionKeyExtensionsConverter()
			),
			messageSerializer
		);

		this.sut = new DeserializeMessageFailureMessageCategoryFallback(this.messageSerializer);
	}

	@Example
	@Domain(EventFixtures.class)
	void fallbackSimpleEvent(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);

		// when
		Message actual = this.sut.fallback(cloudEvent, new RuntimeException("deserialize failed"));

		//then
		assertThat(actual).isExactlyInstanceOf(SimpleEvent.class);

		SimpleEvent simpleEvent = (SimpleEvent)actual;
		assertThat(simpleEvent.getId()).isEqualTo(testDomainEvent.getId());
		assertThat(simpleEvent.getSourceId()).isEqualTo(testDomainEvent.getSourceId());
		assertThat(simpleEvent.getSourceVersion()).isEqualTo(testDomainEvent.getSourceVersion());
		assertThat(simpleEvent.getSourceType()).isEqualTo(testDomainEvent.getSourceType());
		assertThat(simpleEvent.getSource()).isEqualTo(testDomainEvent.getSource());
		assertThat(simpleEvent.getDataSchema()).isEqualTo(testDomainEvent.getDataSchema());
		assertThat(simpleEvent.getSubject()).isEqualTo(testDomainEvent.getSubject());
		assertThat(simpleEvent.getPartitionKey()).isEqualTo(testDomainEvent.getPartitionKey());
		assertThat(simpleEvent.getCorrelationId()).isEqualTo(testDomainEvent.getCorrelationId());
		assertThat(simpleEvent.getOperationId()).isEqualTo(testDomainEvent.getOperationId());
		assertThat(simpleEvent.getOccurrenceTime())
			.isCloseTo(testDomainEvent.getOccurrenceTime(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(simpleEvent.getOperationId()).isEqualTo(testDomainEvent.getOperationId());
		assertThat(simpleEvent.getPayload()).hasSize(1);
		assertThat(simpleEvent.get("name")).isEqualTo(testDomainEvent.getName());
	}

	@Example
	@Domain(CommandFixtures.class)
	void fallbackCommand(@ForAll TestCommand testCommand) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testCommand);

		// when
		Message actual = this.sut.fallback(cloudEvent, new RuntimeException("deserialize failed"));

		//then
		assertThat(actual).isExactlyInstanceOf(SimpleCommand.class);

		SimpleCommand simpleCommand = (SimpleCommand)actual;
		assertThat(simpleCommand.getId()).isEqualTo(testCommand.getId());
		assertThat(simpleCommand.getSourceId()).isEqualTo(testCommand.getSourceId());
		assertThat(simpleCommand.getSourceVersion()).isEqualTo(testCommand.getSourceVersion());
		assertThat(simpleCommand.getSourceType()).isEqualTo(testCommand.getSourceType());
		assertThat(simpleCommand.getSource()).isEqualTo(testCommand.getSource());
		assertThat(simpleCommand.getDataSchema()).isEqualTo(testCommand.getDataSchema());
		assertThat(simpleCommand.getSubject()).isEqualTo(testCommand.getSubject());
		assertThat(simpleCommand.getPartitionKey()).isEqualTo(testCommand.getPartitionKey());
		assertThat(simpleCommand.getCorrelationId()).isEqualTo(testCommand.getCorrelationId());
		assertThat(simpleCommand.getOperationId()).isEqualTo(testCommand.getOperationId());
		assertThat(simpleCommand.getOccurrenceTime())
			.isCloseTo(testCommand.getOccurrenceTime(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(simpleCommand.getOperationId()).isEqualTo(testCommand.getOperationId());
		assertThat(simpleCommand.getPayload()).hasSize(1);
		assertThat(simpleCommand.get("name")).isEqualTo(testCommand.getName());
	}

	@Example
	@Domain(MessageFixtures.class)
	void fallbackMessage(@ForAll TestPartitioned testPartitioned) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testPartitioned);

		// when
		Message actual = this.sut.fallback(cloudEvent, new RuntimeException("deserialize failed"));

		//then
		assertThat(actual).isExactlyInstanceOf(SimpleMessage.class);

		SimpleMessage simpleMessage = (SimpleMessage)actual;
		assertThat(simpleMessage.getId()).isEqualTo(testPartitioned.getId());
		assertThat(simpleMessage.getSourceId()).isEqualTo(testPartitioned.getSourceId());
		assertThat(simpleMessage.getSourceVersion()).isEqualTo(testPartitioned.getSourceVersion());
		assertThat(simpleMessage.getSourceType()).isEqualTo(testPartitioned.getSourceType());
		assertThat(simpleMessage.getSource()).isEqualTo(testPartitioned.getSource());
		assertThat(simpleMessage.getDataSchema()).isEqualTo(testPartitioned.getDataSchema());
		assertThat(simpleMessage.getSubject()).isEqualTo(testPartitioned.getSubject());
		assertThat(simpleMessage.getPartitionKey()).isEqualTo(testPartitioned.getPartitionKey());
		assertThat(simpleMessage.getCorrelationId()).isEqualTo(testPartitioned.getCorrelationId());
		assertThat(simpleMessage.getOperationId()).isEqualTo(testPartitioned.getOperationId());
		assertThat(simpleMessage.getOccurrenceTime())
			.isCloseTo(simpleMessage.getOccurrenceTime(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(simpleMessage.getOperationId()).isEqualTo(testPartitioned.getOperationId());
		assertThat(simpleMessage.getPayload()).hasSize(1);
		assertThat(simpleMessage.get("name")).isEqualTo(testPartitioned.getName());
	}
}
