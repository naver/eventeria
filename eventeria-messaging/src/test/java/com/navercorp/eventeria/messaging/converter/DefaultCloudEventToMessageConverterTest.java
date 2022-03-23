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

import java.time.temporal.ChronoUnit;

import org.assertj.core.data.TemporalUnitWithinOffset;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeTry;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.extension.CloudEventTypeAliasExtension;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonCloudEventSerializer;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class DefaultCloudEventToMessageConverterTest {
	private final JacksonMessageSerializer messageSerializer = new JacksonMessageSerializer();
	private final JacksonCloudEventSerializer cloudEventSerializer = new JacksonCloudEventSerializer();
	private MessageToCloudEventConverter messageToCloudEventConverter;
	private DefaultCloudEventToMessageConverter sut;

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

		this.sut = new DefaultCloudEventToMessageConverter(
			typeAliasMapper,
			messageSerializer
		);
	}

	@Example
	@Domain(EventFixtures.class)
	void convert(@ForAll TestDomainEvent testDomainEvent) {
		// given
		testDomainEvent.appendExtension("hello", "world");
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);

		// when
		Message actual = sut.convert(cloudEvent);

		// then
		assertThat(actual).isInstanceOf(TestDomainEvent.class);

		TestDomainEvent actualEvent = (TestDomainEvent)actual;
		assertThat(actualEvent.getName()).isEqualTo(testDomainEvent.getName());
		assertThat(actualEvent.getId()).isEqualTo(testDomainEvent.getId());
		assertThat(actualEvent.getSourceId()).isEqualTo(testDomainEvent.getSourceId());
		assertThat(actualEvent.getSourceVersion()).isEqualTo(testDomainEvent.getSourceVersion());
		assertThat(actualEvent.getCorrelationId()).isEqualTo(testDomainEvent.getCorrelationId());
		assertThat(actualEvent.getOperationId()).isEqualTo(testDomainEvent.getOperationId());
		assertThat(actualEvent.getOccurrenceTime()).isEqualTo(testDomainEvent.getOccurrenceTime());
		assertThat(actualEvent.getExtensionNames())
			.containsExactlyInAnyOrderElementsOf(testDomainEvent.getExtensionNames());

		actualEvent.getExtensionNames().forEach(it ->
			assertThat(actualEvent.getExtension(it)).isEqualTo(testDomainEvent.getExtension(it))
		);
		assertThat(actualEvent.getExtension("hello")).isEqualTo("world");
	}

	@Example
	@Domain(EventFixtures.class)
	void convertWithSerializeDeserialize(@ForAll TestDomainEvent testDomainEvent) {
		// given
		testDomainEvent.appendExtension("hello", "world");
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);
		byte[] serialized = this.cloudEventSerializer.serialize(cloudEvent);
		CloudEvent deserialized = this.cloudEventSerializer.deserialize(serialized);

		// when
		Message actual = sut.convert(deserialized);

		// then
		assertThat(actual).isInstanceOf(TestDomainEvent.class);

		TestDomainEvent actualEvent = (TestDomainEvent)actual;
		assertThat(actualEvent.getName()).isEqualTo(testDomainEvent.getName());
		assertThat(actualEvent.getId()).isEqualTo(testDomainEvent.getId());
		assertThat(actualEvent.getSourceId()).isEqualTo(testDomainEvent.getSourceId());
		assertThat(actualEvent.getSourceVersion()).isEqualTo(testDomainEvent.getSourceVersion());
		assertThat(actualEvent.getCorrelationId()).isEqualTo(testDomainEvent.getCorrelationId());
		assertThat(actualEvent.getOperationId()).isEqualTo(testDomainEvent.getOperationId());
		assertThat(actualEvent.getOccurrenceTime())
			.isCloseTo(testDomainEvent.getOccurrenceTime(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));

		assertThat(actualEvent.getExtensionNames()).containsAll(testDomainEvent.getExtensionNames());
		assertThat(actualEvent.getExtensionNames()).containsOnlyOnce(
			CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION,
			MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION,
			PartitionKeyExtension.PARTITION_KEY_EXTENSION
		);

		testDomainEvent.getExtensionNames().forEach(it ->
			assertThat(actualEvent.getExtension(it)).isEqualTo(testDomainEvent.getExtension(it))
		);

		assertThat(actualEvent.getExtensionNames()).containsExactlyInAnyOrderElementsOf(cloudEvent.getExtensionNames());
		actualEvent.getExtensionNames().forEach(it ->
			assertThat(actualEvent.getExtension(it)).isEqualTo(cloudEvent.getExtension(it))
		);

		assertThat(actualEvent.getExtension("hello")).isEqualTo("world");
	}
}
