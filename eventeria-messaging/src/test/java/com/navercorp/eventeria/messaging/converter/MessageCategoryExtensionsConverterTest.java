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

import io.cloudevents.CloudEventExtensions;

import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension;
import com.navercorp.eventeria.messaging.fixture.CommandFixtures;
import com.navercorp.eventeria.messaging.fixture.CommandFixtures.TestCommand;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.MessageFixtures;
import com.navercorp.eventeria.messaging.fixture.MessageFixtures.TestMessage;
import com.navercorp.eventeria.messaging.fixture.MessageFixtures.TestTimerMessage;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.fixture.TestEvent;
import com.navercorp.eventeria.messaging.fixture.TestIntegrationEvent;

class MessageCategoryExtensionsConverterTest {
	@Example
	@Domain(MessageFixtures.class)
	void convertMessage(@ForAll TestMessage message) {
		// given
		CloudEventExtensionsConverter sut = new MessageCategoryExtensionsConverter();

		// when
		CloudEventExtensions actual = sut.convert(message);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE");
	}

	@Example
	@Domain(MessageFixtures.class)
	void convertTimerMessage(@ForAll TestTimerMessage timerMessage) {
		// given
		CloudEventExtensionsConverter sut = new MessageCategoryExtensionsConverter();

		// when
		CloudEventExtensions actual = sut.convert(timerMessage);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,TIMER");
	}

	@Example
	@Domain(EventFixtures.class)
	void convertEvent(@ForAll TestEvent event) {
		// given
		CloudEventExtensionsConverter sut = new MessageCategoryExtensionsConverter();

		// when
		CloudEventExtensions actual = sut.convert(event);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,EVENT");
	}

	@Example
	@Domain(EventFixtures.class)
	void convertDomainEvent(@ForAll TestDomainEvent domainEvent) {
		// given
		CloudEventExtensionsConverter sut = new MessageCategoryExtensionsConverter();

		// when
		CloudEventExtensions actual = sut.convert(domainEvent);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,EVENT,DOMAIN_EVENT");
	}

	@Example
	@Domain(EventFixtures.class)
	void convertIntegrationEvent(@ForAll TestIntegrationEvent integrationEvent) {
		// given
		CloudEventExtensionsConverter sut = new MessageCategoryExtensionsConverter();

		// when
		CloudEventExtensions actual = sut.convert(integrationEvent);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,EVENT,INTEGRATION_EVENT");
	}

	@Example
	@Domain(CommandFixtures.class)
	void convertCommand(@ForAll TestCommand command) {
		// given
		CloudEventExtensionsConverter sut = new MessageCategoryExtensionsConverter();

		// when
		CloudEventExtensions actual = sut.convert(command);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,COMMAND");
	}

	@Example
	@Domain(EventFixtures.class)
	void convertIgnoreExistExtensions(@ForAll TestDomainEvent domainEvent) {
		// given
		domainEvent.appendExtension(
			MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION,
			"MESSAGE,EVENT"
		);
		CloudEventExtensionsConverter sut = new MessageCategoryExtensionsConverter();

		// when
		CloudEventExtensions actual = sut.convert(domainEvent);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,EVENT,DOMAIN_EVENT");
	}
}
