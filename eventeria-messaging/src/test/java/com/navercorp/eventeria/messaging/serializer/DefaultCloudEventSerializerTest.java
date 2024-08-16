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

package com.navercorp.eventeria.messaging.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeTry;

import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.format.ContentType;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.core.v1.CloudEventV1;
import io.cloudevents.jackson.JsonCloudEventData;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.CloudEventTypeAliasExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.CompositeCloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.MessageCategoryExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.PartitionKeyExtensionsConverter;
import com.navercorp.eventeria.messaging.extension.CloudEventTypeAliasExtension;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class DefaultCloudEventSerializerTest {
	private final DefaultCloudEventSerializer sut = new DefaultCloudEventSerializer(
		EventFormatProvider.getInstance().resolveFormat(ContentType.JSON)
	);
	private final JacksonMessageSerializer messageSerializer = new JacksonMessageSerializer();
	private MessageToCloudEventConverter messageToCloudEventConverter;
	private CloudEventToMessageConverter cloudEventToMessageConverter;

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

		this.cloudEventToMessageConverter = new DefaultCloudEventToMessageConverter(
			typeAliasMapper,
			messageSerializer
		);
	}

	@Example
	@Domain(EventFixtures.class)
	void serializeAndDeserialize(@ForAll TestDomainEvent testDomainEvent) {
		// given
		int extensionSize = testDomainEvent.getExtensionNames().size();
		testDomainEvent.appendExtension("hello", "world");

		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);
		byte[] serialized = this.sut.serialize(cloudEvent);

		// when
		CloudEvent actual = this.sut.deserialize(serialized);

		// then
		assertThat(actual.getSpecVersion()).isEqualTo(SpecVersion.V1);
		assertThat(actual.getAttribute(CloudEventV1.SPECVERSION)).isEqualTo(SpecVersion.V1);
		assertThat(actual.getId()).isEqualTo(testDomainEvent.getId().toString());
		assertThat(actual.getAttribute(CloudEventV1.ID)).isEqualTo(testDomainEvent.getId().toString());
		assertThat(actual.getType()).isEqualTo(testDomainEvent.getClass().getName());
		assertThat(actual.getAttribute(CloudEventV1.TYPE)).isEqualTo(testDomainEvent.getClass().getName());
		assertThat(actual.getSource()).isEqualTo(testDomainEvent.getSource());
		assertThat(actual.getAttribute(CloudEventV1.SOURCE)).isEqualTo(testDomainEvent.getSource());
		assertThat(actual.getDataContentType()).isEqualTo("application/json");
		assertThat(actual.getAttribute(CloudEventV1.DATACONTENTTYPE)).isEqualTo("application/json");
		assertThat(actual.getDataSchema()).isEqualTo(testDomainEvent.getDataSchema().orElse(null));
		assertThat(actual.getAttribute(CloudEventV1.DATASCHEMA))
			.isEqualTo(testDomainEvent.getDataSchema().orElse(null));
		assertThat(actual.getSubject()).isEqualTo(testDomainEvent.getSubject().orElse(null));
		assertThat(actual.getAttribute(CloudEventV1.SUBJECT)).isEqualTo(testDomainEvent.getSubject().orElse(null));
		assertThat(actual.getTime()).isEqualTo(testDomainEvent.getOccurrenceTime());
		assertThat(actual.getAttribute(CloudEventV1.TIME)).isEqualTo(testDomainEvent.getOccurrenceTime());

		assertThat(actual.getExtensionNames()).hasSize(4 + extensionSize);
		assertThat(actual.getExtensionNames()).containsOnlyOnce(
			CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION,
			MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION,
			PartitionKeyExtension.PARTITION_KEY_EXTENSION,
			"hello"
		);
		assertThat(actual.getExtension(CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION))
			.isEqualTo("deserializedType");
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,EVENT,DOMAIN_EVENT");
		assertThat(actual.getExtension(PartitionKeyExtension.PARTITION_KEY_EXTENSION))
			.isEqualTo(testDomainEvent.getPartitionKey());
		assertThat(actual.getExtension("hello")).isEqualTo("world");

		assertThat(actual.getData()).isInstanceOf(JsonCloudEventData.class);

		Message message = this.cloudEventToMessageConverter.convert(actual);
		assertThat(message).isInstanceOf(TestDomainEvent.class);

		TestDomainEvent actualMessage = (TestDomainEvent)message;
		assertThat(actualMessage.getId()).isEqualTo(testDomainEvent.getId());
		assertThat(actualMessage.getExtensionNames().size()).isEqualTo(testDomainEvent.getExtensionNames().size() + 3);
		testDomainEvent.getExtensionNames().forEach(it ->
			assertThat(actualMessage.getExtension(it)).isEqualTo(testDomainEvent.getExtension(it))
		);
		assertThat(actualMessage.getExtension("hello")).isEqualTo("world");
		assertThat(actualMessage.getExtension("partitionkey")).isEqualTo(actualMessage.getPartitionKey());
		assertThat(actualMessage.getExtension("typealias")).isEqualTo("deserializedType");
	}
}
