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

package com.navercorp.eventeria.messaging.distribution;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;

import net.jqwik.api.Example;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.distribution.PartitionKeyExtractor;
import com.navercorp.eventeria.messaging.contract.distribution.Partitioned;
import com.navercorp.eventeria.messaging.distribution.DefaultPartitionGeneratorTest.SimpleCloudEvent;
import com.navercorp.eventeria.messaging.distribution.DefaultPartitionGeneratorTest.SimpleMessage;
import com.navercorp.eventeria.messaging.distribution.DefaultPartitionGeneratorTest.SimplePartitioned;

class DefaultPartitionKeyExtractorTest {
	private final DefaultPartitionKeyExtractor sut = new DefaultPartitionKeyExtractor();

	@Example
	void defaultPartitionKeyGeneratorAssignablePartitionKeyExtractor() {
		assertThat(PartitionKeyExtractor.class).isAssignableFrom(DefaultPartitionKeyExtractor.class);
	}

	@Example
	void extractPartitionKey() {
		// given
		String partitionKey = UUID.randomUUID().toString();
		Partitioned partitioned = new SimplePartitioned(partitionKey);

		// when
		Optional<String> actual = this.sut.extractKey(partitioned);

		// then
		assertThat(actual).isPresent();
		assertThat(actual.get()).isEqualTo(partitionKey);
	}

	@Example
	void messageWithoutPartitionedBeExtractDependsOnSource() {
		// given
		UUID messageId = UUID.randomUUID();
		Message message = new SimpleMessage(messageId);

		// when
		Optional<String> actual = this.sut.extractKey(message);

		// then
		assertThat(actual).isPresent();
		assertThat(actual.get()).isEqualTo(message.getSourceId());
	}

	@Example
	void cloudEventExtractPartitionDependsOnData() {
		// given
		String partitionKey = UUID.randomUUID().toString();
		SimplePartitioned partitioned = new SimplePartitioned(partitionKey);
		CloudEvent cloudEvent = new SimpleCloudEvent(partitioned);

		// when
		Optional<String> actual = this.sut.extractKey(cloudEvent);

		// then
		assertThat(actual).isPresent();
		assertThat(actual.get()).isEqualTo(partitionKey);
	}
}
