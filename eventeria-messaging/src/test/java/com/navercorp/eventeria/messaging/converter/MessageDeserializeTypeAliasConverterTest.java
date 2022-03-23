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

package com.navercorp.eventeria.messaging.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeTry;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventMessageConverter;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.fixture.TestIntegrationEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;
import com.navercorp.eventeria.messaging.typealias.MessageTypeAliasNotFoundException;

class MessageDeserializeTypeAliasConverterTest {
	private CloudEventMessageConverter cloudEventMessageConverter;
	private MessageDeserializeTypeAliasConverter sut;

	@BeforeTry
	void setUp() {
		CloudEventMessageTypeAliasMapper typeAliasMapper = new CloudEventMessageTypeAliasMapper();
		typeAliasMapper.addCompatibleTypeAlias(TestDomainEvent.class, "serializedType");
		typeAliasMapper.addSerializeTypeAlias(TestIntegrationEvent.class, "integrationEvent");

		this.sut = new MessageDeserializeTypeAliasConverter(typeAliasMapper);
		JacksonMessageSerializer messageSerializer = new JacksonMessageSerializer();
		this.cloudEventMessageConverter = new DefaultCloudEventMessageConverter(
			new DefaultMessageToCloudEventConverter(
				new DefaultCloudEventAttributesConverter(),
				new CompositeCloudEventExtensionsConverter(
					new CloudEventTypeAliasExtensionsConverter(typeAliasMapper),
					new MessageCategoryExtensionsConverter(),
					new PartitionKeyExtensionsConverter()
				),
				messageSerializer
			),
			new DefaultCloudEventToMessageConverter(this.sut, messageSerializer)
		);
	}

	@Example
	@Domain(EventFixtures.class)
	void convert(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEvent cloudEvent = this.cloudEventMessageConverter.convert(testDomainEvent);

		// when
		Class<? extends Message> actual = this.sut.convert(cloudEvent);

		assertThat(actual).isEqualTo(TestDomainEvent.class);
	}

	@Example
	@Domain(EventFixtures.class)
	void convertTypeAliasNotFoundException(@ForAll TestIntegrationEvent testIntegrationEvent) {
		CloudEvent cloudEvent = this.cloudEventMessageConverter.convert(testIntegrationEvent);

		assertThatThrownBy(() -> this.sut.convert(cloudEvent))
			.isExactlyInstanceOf(MessageTypeAliasNotFoundException.class)
			.extracting(it -> ((MessageTypeAliasNotFoundException)it).getTypeName())
			.isEqualTo("integrationEvent");
	}
}
