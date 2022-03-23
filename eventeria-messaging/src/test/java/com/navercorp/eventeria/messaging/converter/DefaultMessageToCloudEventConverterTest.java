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

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeTry;

import io.cloudevents.CloudEvent;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.data.PojoCloudEventData;
import io.cloudevents.core.v1.CloudEventV1;

import com.navercorp.eventeria.messaging.extension.CloudEventTypeAliasExtension;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class DefaultMessageToCloudEventConverterTest {
	private final JacksonMessageSerializer messageSerializer = new JacksonMessageSerializer();
	private DefaultMessageToCloudEventConverter sut;

	@BeforeTry
	void setUp() {
		CloudEventMessageTypeAliasMapper typeAliasMapper = new CloudEventMessageTypeAliasMapper();
		typeAliasMapper.addSerializeTypeAlias(TestDomainEvent.class, "serializedType");
		this.sut = new DefaultMessageToCloudEventConverter(
			new DefaultCloudEventAttributesConverter(),
			new CompositeCloudEventExtensionsConverter(
				new CloudEventTypeAliasExtensionsConverter(typeAliasMapper),
				new MessageCategoryExtensionsConverter(),
				new PartitionKeyExtensionsConverter()
			),
			messageSerializer
		);
	}

	@Example
	@Domain(EventFixtures.class)
	void convert(@ForAll TestDomainEvent testDomainEvent) {
		// given
		int extensionSize = testDomainEvent.getExtensionNames().size();
		testDomainEvent.appendExtension("hello", "world");
		testDomainEvent.appendExtension("test", null);
		testDomainEvent.appendExtension("TestUpper", "testValue");

		// when
		CloudEvent actual = this.sut.convert(testDomainEvent);

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

		assertThat(actual.getExtensionNames()).hasSize(5 + extensionSize);
		assertThat(actual.getExtensionNames()).containsOnlyOnce(
			CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION,
			MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION,
			PartitionKeyExtension.PARTITION_KEY_EXTENSION,
			"hello",
			"testupper"
		);
		assertThat(actual.getExtension(CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION))
			.isEqualTo("serializedType");
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,EVENT,DOMAIN_EVENT");
		assertThat(actual.getExtension(PartitionKeyExtension.PARTITION_KEY_EXTENSION))
			.isEqualTo(testDomainEvent.getPartitionKey());
		assertThat(actual.getExtension("hello")).isEqualTo("world");
		assertThat(actual.getExtension("test")).isNull();
		assertThat(actual.getExtension("testupper")).isEqualTo("testValue");
		assertThat(actual.getExtension("TestUpper")).isNull();

		assertThat(actual.getData()).isInstanceOf(PojoCloudEventData.class);

		PojoCloudEventData<?> pojoCloudEventData = (PojoCloudEventData<?>)actual.getData();
		assertThat(pojoCloudEventData).isNotNull();
		assertThat(pojoCloudEventData.getValue()).isEqualTo(testDomainEvent);

		byte[] serializedData = pojoCloudEventData.toBytes();
		TestDomainEvent deserialized = messageSerializer.deserialize(serializedData, TestDomainEvent.class);
		assertThat(deserialized.getId()).isEqualTo(testDomainEvent.getId());
	}
}
