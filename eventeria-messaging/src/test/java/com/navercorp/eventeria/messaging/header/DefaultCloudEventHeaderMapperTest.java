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

package com.navercorp.eventeria.messaging.header;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.converter.CloudEventTypeAliasExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.CompositeCloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.jackson.header.JacksonCloudEventHeaderMapper;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class DefaultCloudEventHeaderMapperTest {
	private final JacksonCloudEventHeaderMapper sut = new JacksonCloudEventHeaderMapper();

	@Example
	@Domain(EventFixtures.class)
	void toHeaderMap(@ForAll TestDomainEvent testDomainEvent) {
		// given
		DefaultMessageToCloudEventConverter cloudEventConverter = new DefaultMessageToCloudEventConverter(
			new DefaultCloudEventAttributesConverter(),
			new CompositeCloudEventExtensionsConverter(),
			new JacksonMessageSerializer()
		);
		CloudEvent cloudEvent = cloudEventConverter.convert(testDomainEvent);

		// when
		Map<String, Object> actual = this.sut.toHeaderMap(cloudEvent);

		// then
		assertThat(actual).hasSize(5);
		assertThat(actual.get("content-type")).isEqualTo("application/cloudevents+json");
		assertThat(actual.get("ce_id")).isEqualTo(cloudEvent.getId());
		assertThat(actual.get("ce_time")).isEqualTo(cloudEvent.getTime().toString());
		assertThat(actual.get("ce_type")).isEqualTo(cloudEvent.getType());
		assertThat(actual.get("ce_partitionkey")).isEqualTo(testDomainEvent.getPartitionKey());
	}

	@Example
	@Domain(EventFixtures.class)
	void toHeaderMapWithTypeAlias(@ForAll TestDomainEvent testDomainEvent) {
		// given
		CloudEventMessageTypeAliasMapper aliasMapper = new CloudEventMessageTypeAliasMapper();
		aliasMapper.addSerializeTypeAlias(TestDomainEvent.class, "serialized");
		DefaultMessageToCloudEventConverter cloudEventConverter = new DefaultMessageToCloudEventConverter(
			new DefaultCloudEventAttributesConverter(),
			new CompositeCloudEventExtensionsConverter(
				new CloudEventTypeAliasExtensionsConverter(aliasMapper)
			),
			new JacksonMessageSerializer()
		);
		CloudEvent cloudEvent = cloudEventConverter.convert(testDomainEvent);

		// when
		Map<String, Object> actual = this.sut.toHeaderMap(cloudEvent);

		// then
		assertThat(actual).hasSize(5);
		assertThat(actual.get("content-type")).isEqualTo("application/cloudevents+json");
		assertThat(actual.get("ce_id")).isEqualTo(cloudEvent.getId());
		assertThat(actual.get("ce_time")).isEqualTo(cloudEvent.getTime().toString());
		assertThat(actual.get("ce_type")).isEqualTo(cloudEvent.getType());
		assertThat(actual.get("ce_typealias")).isEqualTo("serialized");
	}
}
