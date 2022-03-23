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

package com.navercorp.eventeria.messaging.extension;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;
import net.jqwik.api.lifecycle.BeforeTry;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.CloudEventTypeAliasExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.fixture.TestIntegrationEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class CloudEventTypeAliasExtensionTest {
	private MessageToCloudEventConverter messageToCloudEventConverter;

	@BeforeTry
	void setUp() {
		CloudEventMessageTypeAliasMapper cloudEventMessageTypeAliasMapper = new CloudEventMessageTypeAliasMapper();
		cloudEventMessageTypeAliasMapper.addSerializeTypeAlias(TestDomainEvent.class, "serializedType");

		this.messageToCloudEventConverter = new DefaultMessageToCloudEventConverter(
			new DefaultCloudEventAttributesConverter(),
			new CloudEventTypeAliasExtensionsConverter(cloudEventMessageTypeAliasMapper),
			new JacksonMessageSerializer()
		);
	}

	@Example
	@Domain(EventFixtures.class)
	void parse(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);

		// when
		CloudEventTypeAliasExtension actual = CloudEventTypeAliasExtension.parseExtension(cloudEvent);

		// then
		assertThat(actual.getTypeAlias())
			.isEqualTo(cloudEvent.getExtension(CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION));
		assertThat(actual.getTypeAlias()).isEqualTo("serializedType");
	}

	@Example
	@Domain(EventFixtures.class)
	void parseNotAlias(@ForAll TestIntegrationEvent testIntegrationEvent) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testIntegrationEvent);

		// when
		CloudEventTypeAliasExtension actual = CloudEventTypeAliasExtension.parseExtension(cloudEvent);

		// then
		assertThat(actual.getTypeAlias()).isNull();
	}

	@Example
	@Domain(EventFixtures.class)
	void parseMessageExtension(@ForAll TestDomainEvent testDomainEvent) {
		// given
		testDomainEvent.appendExtension(
			CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION,
			"serializedType"
		);

		// when
		CloudEventTypeAliasExtension actual = CloudEventTypeAliasExtension.parseExtension(testDomainEvent);

		// then
		assertThat(actual.getTypeAlias()).isEqualTo("serializedType");
	}

	@Example
	@Domain(EventFixtures.class)
	void parseMessageExtensionNotAlias(@ForAll TestIntegrationEvent testDomainEvent) {
		// when
		CloudEventTypeAliasExtension actual = CloudEventTypeAliasExtension.parseExtension(testDomainEvent);

		// then
		assertThat(actual.getTypeAlias()).isNull();
	}
}
