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

package com.navercorp.eventeria.messaging.spring;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import org.assertj.core.data.TemporalUnitWithinOffset;
import org.springframework.messaging.MessageHeaders;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;

import io.cloudevents.CloudEvent;
import io.cloudevents.spring.messaging.CloudEventMessageConverter;

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
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.spring.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.spring.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class CloudEventMessageConverterTest {
	private final MessageToCloudEventConverter messageToCloudEventConverter = new DefaultMessageToCloudEventConverter(
		new DefaultCloudEventAttributesConverter(),
		new CompositeCloudEventExtensionsConverter(
			new CloudEventTypeAliasExtensionsConverter(new CloudEventMessageTypeAliasMapper()),
			new MessageCategoryExtensionsConverter(),
			new PartitionKeyExtensionsConverter()
		),
		new JacksonMessageSerializer()
	);
	private final CloudEventToMessageConverter cloudEventToMessageConverter = new DefaultCloudEventToMessageConverter(
		new CloudEventMessageTypeAliasMapper(),
		new JacksonMessageSerializer()
	);

	// send
	@Example
	@Domain(EventFixtures.class)
	void toMessage(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEventMessageConverter sut = new CloudEventMessageConverter();

		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);

		// when
		org.springframework.messaging.Message<?> springMessage = sut.toMessage(
			cloudEvent,
			new MessageHeaders(Collections.singletonMap("hello", "world"))
		);

		// then
		assertThat(cloudEvent.getData().toBytes()).isEqualTo(springMessage.getPayload());

		MessageHeaders headers = springMessage.getHeaders();
		assertThat(headers.get("hello")).isEqualTo("world");
		assertThat(headers.get("ce-id")).isEqualTo(cloudEvent.getId());
		assertThat(headers.get("ce-specversion")).isEqualTo(cloudEvent.getSpecVersion().toString());
		assertThat(headers.get("ce-type")).isEqualTo(cloudEvent.getType());
		assertThat(headers.get("ce-source")).isEqualTo(cloudEvent.getSource().toString());
		assertThat(headers.get("ce-datacontenttype")).isEqualTo(cloudEvent.getDataContentType());
		assertThat(headers.get("ce-dataschema"))
			.isEqualTo(cloudEvent.getDataSchema() != null ? cloudEvent.getDataSchema().toString() : null);
		assertThat(headers.get("ce-subject")).isEqualTo(cloudEvent.getSubject());
		assertThat(headers.get("ce-time")).isEqualTo(
			DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(cloudEvent.getTime()));
		cloudEvent.getExtensionNames().forEach(it ->
			assertThat(headers.get("ce-" + it)).isEqualTo(cloudEvent.getExtension(it))
		);
	}

	// receive
	@Example
	@Domain(EventFixtures.class)
	void fromMessage(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEventMessageConverter sut = new CloudEventMessageConverter();

		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);
		org.springframework.messaging.Message<?> springMessage = sut.toMessage(
			cloudEvent,
			new MessageHeaders(Collections.singletonMap("hello", "world"))
		);

		// when
		Object actual = sut.fromMessage(springMessage, CloudEvent.class);

		// then
		assertThat(actual).isInstanceOf(CloudEvent.class);

		CloudEvent actualEvent = (CloudEvent)actual;
		assertThat(actualEvent.getData().toBytes()).isEqualTo(cloudEvent.getData().toBytes());
		assertThat(actualEvent.getId()).isEqualTo(cloudEvent.getId());
		assertThat(actualEvent.getSpecVersion()).isEqualTo(cloudEvent.getSpecVersion());
		assertThat(actualEvent.getType()).isEqualTo(cloudEvent.getType());
		assertThat(actualEvent.getSource()).isEqualTo(cloudEvent.getSource());
		assertThat(actualEvent.getDataContentType()).isEqualTo(cloudEvent.getDataContentType());
		assertThat(actualEvent.getDataSchema()).isEqualTo(cloudEvent.getDataSchema());
		assertThat(actualEvent.getSubject()).isEqualTo(cloudEvent.getSubject());
		assertThat(actualEvent.getTime()).isCloseTo(
			cloudEvent.getTime(),
			new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
		);
		assertThat(actualEvent.getExtensionNames().size()).isEqualTo(cloudEvent.getExtensionNames().size());
		actualEvent.getExtensionNames().forEach(it ->
			assertThat(actualEvent.getExtension(it)).isEqualTo(cloudEvent.getExtension(it))
		);

		Message actualMessage = this.cloudEventToMessageConverter.convert(cloudEvent);
		assertThat(actualMessage).isInstanceOf(TestDomainEvent.class);

		TestDomainEvent actualTestDomainEvent = (TestDomainEvent)actualMessage;
		assertThat(actualTestDomainEvent.getId()).isEqualTo(testDomainEvent.getId());
		assertThat(actualTestDomainEvent.getSourceId()).isEqualTo(testDomainEvent.getSourceId());
		assertThat(actualTestDomainEvent.getSourceVersion()).isEqualTo(testDomainEvent.getSourceVersion());
		assertThat(actualTestDomainEvent.getCorrelationId()).isEqualTo(testDomainEvent.getCorrelationId());
		assertThat(actualTestDomainEvent.getOperationId()).isEqualTo(testDomainEvent.getOperationId());
		assertThat(actualTestDomainEvent.getOccurrenceTime()).isEqualTo(testDomainEvent.getOccurrenceTime());
		assertThat(actualTestDomainEvent.getDataSchema()).isEqualTo(testDomainEvent.getDataSchema());
		assertThat(actualTestDomainEvent.getSubject()).isEqualTo(testDomainEvent.getSubject());
		assertThat(actualTestDomainEvent.getPartitionKey()).isEqualTo(testDomainEvent.getPartitionKey());
		assertThat(actualTestDomainEvent.getName()).isEqualTo(testDomainEvent.getName());
		assertThat(actualTestDomainEvent.getExtensionNames().size())
			.isEqualTo(testDomainEvent.getExtensionNames().size());
		actualTestDomainEvent.getExtensionNames().forEach(it ->
			assertThat(actualTestDomainEvent.getExtension(it)).isEqualTo(testDomainEvent.getExtension(it))
		);
	}
}
