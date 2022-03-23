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

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.converter.CloudEventTypeAliasExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.CompositeCloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventMessageConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.MessageCategoryExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.PartitionKeyExtensionsConverter;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonCloudEventSerializer;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class DefaultCloudEventMessageReaderWriterTest {
	private DefaultCloudEventMessageReaderWriter sut;

	@BeforeTry
	void setUp() {
		CloudEventMessageTypeAliasMapper typeAliasMapper = new CloudEventMessageTypeAliasMapper();
		typeAliasMapper.addCompatibleTypeAlias(TestDomainEvent.class, "deserializedType");

		JacksonMessageSerializer messageSerializer = new JacksonMessageSerializer();

		this.sut = new DefaultCloudEventMessageReaderWriter(
			new DefaultCloudEventMessageConverter(
				new DefaultMessageToCloudEventConverter(
					new DefaultCloudEventAttributesConverter(),
					new CompositeCloudEventExtensionsConverter(
						new CloudEventTypeAliasExtensionsConverter(typeAliasMapper),
						new MessageCategoryExtensionsConverter(),
						new PartitionKeyExtensionsConverter()
					),
					messageSerializer
				),
				new DefaultCloudEventToMessageConverter(
					typeAliasMapper,
					messageSerializer
				)
			),
			new JacksonCloudEventSerializer()
		);
	}

	@Example
	@Domain(EventFixtures.class)
	void writeRead(@ForAll TestDomainEvent testDomainEvent) {
		// given
		int extensionSize = testDomainEvent.getExtensionNames().size();
		testDomainEvent.appendExtension("hello", "world");

		byte[] serialized = this.sut.write(testDomainEvent);

		// when
		Message actual = this.sut.read(serialized);

		// then
		assertThat(actual).isInstanceOf(TestDomainEvent.class);

		TestDomainEvent actualMessage = (TestDomainEvent)actual;
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
