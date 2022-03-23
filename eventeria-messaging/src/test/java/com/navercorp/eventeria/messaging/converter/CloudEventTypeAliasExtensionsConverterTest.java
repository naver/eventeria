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

import java.util.UUID;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;

import io.cloudevents.CloudEventExtensions;

import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.extension.EmptyCloudEventExtensions;
import com.navercorp.eventeria.messaging.extension.CloudEventTypeAliasExtension;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class CloudEventTypeAliasExtensionsConverterTest {
	@Example
	@Domain(EventFixtures.class)
	void convert(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEventMessageTypeAliasMapper serializeTypeAliasMapper = new CloudEventMessageTypeAliasMapper();
		serializeTypeAliasMapper.addSerializeTypeAlias(testDomainEvent.getClass(), "serializeType");
		CloudEventExtensionsConverter sut = new CloudEventTypeAliasExtensionsConverter(serializeTypeAliasMapper);

		// when
		CloudEventExtensions actual = sut.convert(testDomainEvent);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION)).isEqualTo("serializeType");
	}

	@Example
	@Domain(EventFixtures.class)
	void convertNotHasAlias(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEventMessageTypeAliasMapper serializeTypeAliasMapper = new CloudEventMessageTypeAliasMapper();
		CloudEventExtensionsConverter sut = new CloudEventTypeAliasExtensionsConverter(serializeTypeAliasMapper);

		// when
		CloudEventExtensions actual = sut.convert(testDomainEvent);

		// then
		assertThat(actual.getExtensionNames()).hasSize(0);
		assertThat(actual.getExtension(CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION)).isNull();
		assertThat(actual).isSameAs(EmptyCloudEventExtensions.INSTANCE);
	}

	@Example
	@Domain(EventFixtures.class)
	void convertIgnoreExistExtensions(@ForAll TestDomainEvent testDomainEvent) {
		// given
		testDomainEvent.appendExtension(
			CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION,
			UUID.randomUUID().toString()
		);

		CloudEventMessageTypeAliasMapper serializeTypeAliasMapper = new CloudEventMessageTypeAliasMapper();
		serializeTypeAliasMapper.addSerializeTypeAlias(testDomainEvent.getClass(), "serializeType");
		CloudEventExtensionsConverter sut = new CloudEventTypeAliasExtensionsConverter(serializeTypeAliasMapper);

		// when
		CloudEventExtensions actual = sut.convert(testDomainEvent);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION)).isEqualTo("serializeType");
	}
}
