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

import java.util.Collections;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;

import io.cloudevents.CloudEventExtensions;

import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.extension.CloudEventTypeAliasExtension;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class CompositeCloudEventExtensionsConverterTest {
	@Example
	@Domain(EventFixtures.class)
	void convert(@ForAll TestDomainEvent testDomainEvent) {
		// given
		int extensionSize = testDomainEvent.getExtensionNames().size();
		testDomainEvent.appendExtension("hello", "world");

		CloudEventMessageTypeAliasMapper typeAliasMapper = new CloudEventMessageTypeAliasMapper();
		typeAliasMapper.addSerializeTypeAlias(TestDomainEvent.class, "serializedType");
		CloudEventExtensionsConverter sut = new CompositeCloudEventExtensionsConverter(
			new CloudEventTypeAliasExtensionsConverter(typeAliasMapper),
			new MessageCategoryExtensionsConverter(),
			new PartitionKeyExtensionsConverter()
		);

		// when
		CloudEventExtensions actual = sut.convert(testDomainEvent);

		// then
		assertThat(actual.getExtensionNames()).hasSize(4 + extensionSize);
		assertThat(actual.getExtensionNames()).containsOnlyOnce(
			CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION,
			MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION,
			PartitionKeyExtension.PARTITION_KEY_EXTENSION,
			"hello"
		);
		assertThat(actual.getExtension(CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION))
			.isEqualTo("serializedType");
		assertThat(actual.getExtension(MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION))
			.isEqualTo("MESSAGE,EVENT,DOMAIN_EVENT");
		assertThat(actual.getExtension(PartitionKeyExtension.PARTITION_KEY_EXTENSION))
			.isEqualTo(testDomainEvent.getPartitionKey());
		assertThat(actual.getExtension("hello")).isEqualTo("world");
	}

	@Example
	@Domain(EventFixtures.class)
	void convertEmpty(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEventExtensionsConverter sut = new CompositeCloudEventExtensionsConverter(Collections.emptyList());

		// when
		CloudEventExtensions actual = sut.convert(testDomainEvent);

		// then
		assertThat(actual.getExtensionNames()).hasSize(testDomainEvent.getExtensionNames().size());
	}
}
