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

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.MessageCategoryExtensionsConverter;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension.MessageCategory;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;

class MessageCategoryExtensionTest {
	private final MessageToCloudEventConverter messageToCloudEventConverter = new DefaultMessageToCloudEventConverter(
		new DefaultCloudEventAttributesConverter(),
		new MessageCategoryExtensionsConverter(),
		new JacksonMessageSerializer()
	);

	@Example
	@Domain(EventFixtures.class)
	void parse(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);

		// when
		MessageCategoryExtension actual = MessageCategoryExtension.parseExtension(cloudEvent);

		// then
		assertThat(actual.getMessageCategories())
			.containsOnlyOnce(MessageCategory.MESSAGE, MessageCategory.EVENT, MessageCategory.DOMAIN_EVENT);
	}

	@Example
	@Domain(EventFixtures.class)
	void parseMessageExtension(@ForAll TestDomainEvent testDomainEvent) {
		// given
		testDomainEvent.appendExtension(
			MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION,
			"MESSAGE,EVENT,DOMAIN_EVENT"
		);

		// when
		MessageCategoryExtension actual = MessageCategoryExtension.parseExtension(testDomainEvent);

		// then
		assertThat(actual.getMessageCategories()).hasSize(3);
		assertThat(actual.getMessageCategories())
			.containsOnlyOnce(MessageCategory.MESSAGE, MessageCategory.EVENT, MessageCategory.DOMAIN_EVENT);
	}

	@Example
	@Domain(EventFixtures.class)
	void parseMessageExtensionNotExistCategory(@ForAll TestDomainEvent testDomainEvent) {
		// given
		testDomainEvent.appendExtension(
			MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION,
			"MESSAGE,EVENT,DOMAIN_EVENT,NOT_EXIST"
		);

		// when
		MessageCategoryExtension actual = MessageCategoryExtension.parseExtension(testDomainEvent);

		// then
		assertThat(actual.getMessageCategories()).hasSize(3);
		assertThat(actual.getMessageCategories())
			.containsOnlyOnce(MessageCategory.MESSAGE, MessageCategory.EVENT, MessageCategory.DOMAIN_EVENT);
	}
}
