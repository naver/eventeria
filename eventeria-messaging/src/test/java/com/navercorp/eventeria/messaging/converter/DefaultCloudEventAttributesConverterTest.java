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

import io.cloudevents.CloudEventAttributes;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.v1.CloudEventV1;

import com.navercorp.eventeria.messaging.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.fixture.TestDomainEvent;

class DefaultCloudEventAttributesConverterTest {
	private final DefaultCloudEventAttributesConverter sut = new DefaultCloudEventAttributesConverter();

	@Example
	@Domain(EventFixtures.class)
	void convert(@ForAll TestDomainEvent testDomainEvent) {
		// when
		CloudEventAttributes actual = this.sut.convert(testDomainEvent);

		// then
		assertThat(actual.getSpecVersion()).isEqualTo(SpecVersion.V1);
		assertThat(actual.getAttribute(CloudEventV1.SPECVERSION)).isEqualTo(SpecVersion.V1);
		assertThat(actual.getId()).isEqualTo(testDomainEvent.getId().toString());
		assertThat(actual.getAttribute(CloudEventV1.ID)).isEqualTo(testDomainEvent.getId().toString());
		assertThat(actual.getType()).isEqualTo(testDomainEvent.getClass().getName());
		assertThat(actual.getAttribute(CloudEventV1.TYPE)).isEqualTo(testDomainEvent.getClass().getName());
		assertThat(actual.getSource()).isEqualTo(testDomainEvent.getSource());
		assertThat(actual.getAttribute(CloudEventV1.SOURCE)).isEqualTo(testDomainEvent.getSource());
		assertThat(actual.getDataContentType()).isEqualTo("application/json");
		assertThat(actual.getAttribute(CloudEventV1.DATACONTENTTYPE)).isEqualTo("application/json");
		assertThat(actual.getDataSchema()).isEqualTo(testDomainEvent.getDataSchema().orElse(null));
		assertThat(actual.getAttribute(CloudEventV1.DATASCHEMA))
			.isEqualTo(testDomainEvent.getDataSchema().orElse(null));
		assertThat(actual.getSubject()).isEqualTo(testDomainEvent.getSubject().orElse(null));
		assertThat(actual.getAttribute(CloudEventV1.SUBJECT)).isEqualTo(testDomainEvent.getSubject().orElse(null));
		assertThat(actual.getTime()).isEqualTo(testDomainEvent.getOccurrenceTime());
		assertThat(actual.getAttribute(CloudEventV1.TIME)).isEqualTo(testDomainEvent.getOccurrenceTime());
	}
}
