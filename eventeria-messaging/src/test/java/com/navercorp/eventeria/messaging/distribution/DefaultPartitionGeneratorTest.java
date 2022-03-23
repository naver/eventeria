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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import net.jqwik.api.Example;
import net.jqwik.api.Label;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.SpecVersion;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.distribution.PartitionGenerator;
import com.navercorp.eventeria.messaging.contract.distribution.Partitioned;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;

class DefaultPartitionGeneratorTest {
	private final DefaultPartitionGenerator sut = new DefaultPartitionGenerator();

	@Example
	@Label("DefaultPartitionGenerator 는 PartitionGenerator 를 구현한다.")
	void defaultPartitionGeneratorAssignablePartitionGenerator() {
		assertThat(PartitionGenerator.class).isAssignableFrom(DefaultPartitionGenerator.class);
	}

	@Example
	@Label("numOfPartition 값이 1보다 작으면 Exception 이 발생한다.")
	void numOfPartitionParameterMustBeOverZero() {
		Partitioned payload = new SimplePartitioned(UUID.randomUUID().toString());
		int numOfPartition = 0;
		assertThatThrownBy(() -> this.sut.partition(payload, numOfPartition))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("numOfPartition must be greater than 0");
	}

	@Example
	@Label("같은 partitionKey 는 같은 partition 번호가 할당 된다.")
	void partitionForEqualsPartitionKeyShouldBeReturnSamePartition() {
		// given
		String partitionKey = UUID.randomUUID().toString();
		int numOfPartition = 10;

		Partitioned partitioned1 = new SimplePartitioned(partitionKey);
		Partitioned partitioned2 = new SimplePartitioned(partitionKey);

		// when
		int actual1 = this.sut.partition(partitioned1, numOfPartition);
		int actual2 = this.sut.partition(partitioned2, numOfPartition);

		// then
		assertThat(actual1).isEqualTo(actual2);
	}

	@Example
	@Label("Partitioned 를 구현하지 않은 Message 객체는 source 를 기반으로 partition 을 얻는다.")
	void messageWithoutPartitionedBeGeneratedDependsOnSource() {
		// given
		UUID messageId = UUID.randomUUID();
		int numOfPartition = 10;

		Message message1 = new SimpleMessage(messageId);
		Message message2 = new SimpleMessage(messageId);

		// when
		int actual1 = this.sut.partition(message1, numOfPartition);
		int actual2 = this.sut.partition(message2, numOfPartition);

		// then
		assertThat(actual1).isEqualTo(actual2);
	}

	@Example
	@Label("CloudEvent 는 data 를 기반으로 partitionKey 를 구한다.")
	void cloudEventGeneratePartitionDependsOnData() {
		// given
		String partitionKey = UUID.randomUUID().toString();
		SimplePartitioned partitioned1 = new SimplePartitioned(partitionKey);
		CloudEvent cloudEvent1 = new SimpleCloudEvent(partitioned1);
		SimplePartitioned partitioned2 = new SimplePartitioned(partitionKey);
		CloudEvent cloudEvent2 = new SimpleCloudEvent(partitioned2);
		int numOfPartition = 10;

		// when
		int actual1 = this.sut.partition(cloudEvent1, numOfPartition);
		int actual2 = this.sut.partition(cloudEvent2, numOfPartition);

		// then
		assertThat(actual1).isEqualTo(actual2);
	}

	@Example
	@Label("partitionKey 를 얻을 수 없다면, fallbackPartition 결과를 반환한다.")
	void partitionForUnknownPayloadShouldGenerateWithFallbackPartition() {
		// given
		Object payload = new SimplePayload("name");
		int numOfPartition = 10;
		Supplier<Integer> fallbackPartition = () -> 9;

		// when
		int actual = this.sut.partition(payload, numOfPartition, fallbackPartition);

		// then
		assertThat(actual).isEqualTo(fallbackPartition.get());
	}

	@Example
	@Label("partitionKey 를 얻을 수 없고, fallbackPartition 이 없다면, round robin 으로 partition 할당된다..")
	void partitionForUnknownPayloadAndNoFallbackPartitionBeGenerateRoundRobin() {
		// given
		Object payload = new SimplePayload("name");
		int numOfPartition = 10;

		// when
		int actual1 = this.sut.partition(payload, numOfPartition);
		int actual2 = this.sut.partition(payload, numOfPartition);
		int actual3 = this.sut.partition(payload, numOfPartition);

		// then
		assertThat(actual1).isEqualTo(0);
		assertThat(actual2).isEqualTo(1);
		assertThat(actual3).isEqualTo(2);
	}

	static class SimplePartitioned implements Partitioned {
		private final String partitionKey;

		public SimplePartitioned(String partitionKey) {
			this.partitionKey = partitionKey;
		}

		@Override
		public String getPartitionKey() {
			return this.partitionKey;
		}
	}

	static class SimplePayload {
		private final String name;

		public SimplePayload(String name) {
			this.name = name;
		}
	}

	static class SimpleMessage implements Message {
		private final UUID id = UUID.randomUUID();
		private final UUID sourceId;

		public SimpleMessage(UUID sourceId) {
			this.sourceId = sourceId;
		}

		@Override
		public UUID getId() {
			return this.id;
		}

		@Override
		public OffsetDateTime getOccurrenceTime() {
			return null;
		}

		@Override
		public String getSourceId() {
			return this.sourceId.toString();
		}

		@Nullable
		@Override
		public Long getSourceVersion() {
			return null;
		}

		@Override
		public String getSourceType() {
			return SimplePayload.class.getName();
		}

		@Override
		public Optional<UUID> getCorrelationId() {
			return Optional.empty();
		}

		@Override
		public Optional<String> getOperationId() {
			return Optional.empty();
		}
	}

	static class SimpleCloudEvent implements CloudEvent {
		private final SimplePartitioned data;

		public SimpleCloudEvent(SimplePartitioned data) {
			this.data = data;
		}

		@Override
		public CloudEventData getData() {
			return null;
		}

		@Override
		public SpecVersion getSpecVersion() {
			return null;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public String getType() {
			return null;
		}

		@Override
		public URI getSource() {
			return null;
		}

		@Override
		public String getDataContentType() {
			return null;
		}

		@Override
		public URI getDataSchema() {
			return null;
		}

		@Override
		public String getSubject() {
			return null;
		}

		@Override
		public OffsetDateTime getTime() {
			return null;
		}

		@Override
		public Object getAttribute(String attributeName) throws IllegalArgumentException {
			return null;
		}

		@Override
		public Object getExtension(String extensionName) {
			if (PartitionKeyExtension.PARTITION_KEY_EXTENSION.equals(extensionName)) {
				return this.data.partitionKey;
			}
			return null;
		}

		@Override
		public Set<String> getExtensionNames() {
			return null;
		}
	}
}
