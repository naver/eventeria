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

import com.navercorp.eventeria.messaging.contract.cloudevents.extension.EmptyCloudEventExtensions;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;
import com.navercorp.eventeria.messaging.fixture.MessageFixtures;
import com.navercorp.eventeria.messaging.fixture.MessageFixtures.TestMessage;
import com.navercorp.eventeria.messaging.fixture.MessageFixtures.TestPartitioned;

class PartitionKeyExtensionsConverterTest {
	private final PartitionKeyExtensionsConverter sut = new PartitionKeyExtensionsConverter();

	@Example
	@Domain(MessageFixtures.class)
	void convert(@ForAll TestPartitioned testPartitioned) {
		// when
		CloudEventExtensions actual = this.sut.convert(testPartitioned);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(PartitionKeyExtension.PARTITION_KEY_EXTENSION))
			.isEqualTo(testPartitioned.getPartitionKey());
	}

	@Example
	@Domain(MessageFixtures.class)
	void convertEmpty(@ForAll TestMessage testMessage) {
		// when
		CloudEventExtensions actual = this.sut.convert(testMessage);

		// then
		assertThat(actual.getExtensionNames()).isEmpty();
		assertThat(actual).isSameAs(EmptyCloudEventExtensions.INSTANCE);
	}

	@Example
	@Domain(MessageFixtures.class)
	void convertIgnoreExistExtensions(@ForAll TestPartitioned testPartitioned) {
		// given
		testPartitioned.appendExtension(
			PartitionKeyExtension.PARTITION_KEY_EXTENSION,
			UUID.randomUUID().toString()
		);

		// when
		CloudEventExtensions actual = this.sut.convert(testPartitioned);

		// then
		assertThat(actual.getExtensionNames()).hasSize(1);
		assertThat(actual.getExtension(PartitionKeyExtension.PARTITION_KEY_EXTENSION))
			.isEqualTo(testPartitioned.getPartitionKey());
	}
}
