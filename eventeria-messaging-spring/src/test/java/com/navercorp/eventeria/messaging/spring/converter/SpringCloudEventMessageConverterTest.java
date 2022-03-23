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

package com.navercorp.eventeria.messaging.spring.converter;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeType;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventSerializerDeserializer;
import com.navercorp.eventeria.messaging.converter.CompositeCloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonCloudEventSerializer;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.spring.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.spring.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class SpringCloudEventMessageConverterTest {
	private final MessageToCloudEventConverter messageToCloudEventConverter = new DefaultMessageToCloudEventConverter(
		new DefaultCloudEventAttributesConverter(),
		new CompositeCloudEventExtensionsConverter(),
		new JacksonMessageSerializer()
	);
	private final CloudEventToMessageConverter cloudEventToMessageConverter = new DefaultCloudEventToMessageConverter(
		new CloudEventMessageTypeAliasMapper(),
		new JacksonMessageSerializer()
	);
	private final CloudEventSerializerDeserializer cloudEventSerializerDeserializer = new JacksonCloudEventSerializer();
	private final SpringCloudEventMessageConverter sut = new SpringCloudEventMessageConverter(
		cloudEventSerializerDeserializer
	);

	@Example
	void supports() {
		then(this.sut.supports(CloudEvent.class)).isTrue();
	}

	@Example
	void supportsFalse() {
		then(this.sut.supports(Message.class)).isFalse();
		then(this.sut.supports(org.springframework.messaging.Message.class)).isFalse();
	}

	@Example
	@Domain(EventFixtures.class)
	void toMessage(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);
		Map<String, Object> headers = new HashMap<>();
		headers.put("hello", "world");
		headers.put("foo", "bar");
		headers.put("content-type", "application/cloudevents+json");
		MessageHeaders messageHeaders = new MessageHeaders(headers);

		// when
		org.springframework.messaging.Message<?> actual = this.sut.toMessage(cloudEvent, messageHeaders);

		then(actual.getPayload()).isEqualTo(this.cloudEventSerializerDeserializer.serialize(cloudEvent));
		then(actual.getHeaders().get("hello")).isEqualTo("world");
		then(actual.getHeaders().get("foo")).isEqualTo("bar");
		then(actual.getHeaders().get("content-type")).isEqualTo("application/cloudevents+json");
		then(actual.getHeaders().get("contentType")).isEqualTo(MimeType.valueOf("application/cloudevents+json"));
	}

	@Example
	@Domain(EventFixtures.class)
	void fromMessage(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);
		Map<String, Object> headers = new HashMap<>();
		headers.put("content-type", "application/cloudevents+json");
		MessageHeaders messageHeaders = new MessageHeaders(headers);
		org.springframework.messaging.Message<?> springMessage = this.sut.toMessage(cloudEvent, messageHeaders);

		// when
		CloudEvent actual = (CloudEvent)this.sut.fromMessage(springMessage, CloudEvent.class);

		then(actual.getId()).isEqualTo(testDomainEvent.getId().toString());

		TestDomainEvent deserialized = (TestDomainEvent)this.cloudEventToMessageConverter.convert(actual);
		then(deserialized.getName()).isEqualTo(testDomainEvent.getName());
	}
}
