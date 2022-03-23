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

import static org.assertj.core.api.BDDAssertions.assertThat;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.PartitionKeyExtensionsConverter;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;

class PartitionKeyExtensionTest {
	private final MessageToCloudEventConverter messageToCloudEventConverter = new DefaultMessageToCloudEventConverter(
		new DefaultCloudEventAttributesConverter(),
		new PartitionKeyExtensionsConverter(),
		new JacksonMessageSerializer()
	);

	@Example
	@Domain(EventFixtures.class)
	void parse(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);

		// when
		PartitionKeyExtension actual = PartitionKeyExtension.parseExtension(cloudEvent);

		// then
		assertThat(actual.getPartitionKey())
			.isEqualTo(cloudEvent.getExtension(PartitionKeyExtension.PARTITION_KEY_EXTENSION));
		assertThat(actual.getPartitionKey()).isEqualTo(testDomainEvent.getPartitionKey());
	}

	@Example
	@Domain(EventFixtures.class)
	void parseMessageExtension(@ForAll TestDomainEvent testDomainEvent) {
		// given
		testDomainEvent.appendExtension(
			PartitionKeyExtension.PARTITION_KEY_EXTENSION,
			testDomainEvent.getPartitionKey()
		);

		// when
		PartitionKeyExtension actual = PartitionKeyExtension.parseExtension(testDomainEvent);

		// then
		assertThat(actual.getPartitionKey()).isEqualTo(testDomainEvent.getPartitionKey());
	}
}
