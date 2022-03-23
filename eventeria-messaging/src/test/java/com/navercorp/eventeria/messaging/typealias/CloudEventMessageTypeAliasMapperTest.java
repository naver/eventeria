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

package com.navercorp.eventeria.messaging.typealias;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import net.jqwik.api.Example;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.event.Event;

class CloudEventMessageTypeAliasMapperTest {
	@Example
	void addSerializeTypeAlias() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		String externalCloudEventType = "eventType";
		Class<MappingEvent> internalCloudEventDataType = MappingEvent.class;

		// when
		sut.addSerializeTypeAlias(internalCloudEventDataType, externalCloudEventType);

		// then
		Optional<String> result = sut.getSerializeTypeAlias(internalCloudEventDataType);
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(externalCloudEventType);
	}

	@Example
	void addDeserializeTypeAlias() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		String externalCloudEventType = "eventType";
		Class<MappingEvent> internalEnvelopDataType = MappingEvent.class;

		// when
		sut.addDeserializeTypeAlias(externalCloudEventType, internalEnvelopDataType);

		// then
		Optional<Class<? extends Message>> result = sut.getDeserializeTypeAlias(externalCloudEventType);
		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(internalEnvelopDataType);
	}

	@Example
	void addCompatibleTypeAlias() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		String externalCloudEventType = "eventType";
		Class<MappingEvent> internalEnvelopDataType = MappingEvent.class;

		// when
		sut.addCompatibleTypeAlias(internalEnvelopDataType, externalCloudEventType);

		// then
		Optional<Class<? extends Message>> result1 = sut.getMessageType(externalCloudEventType);
		assertThat(result1).isPresent();
		assertThat(result1.get()).isEqualTo(MappingEvent.class);

		Optional<String> result2 = sut.getMessageAliasType(internalEnvelopDataType);
		assertThat(result2).isPresent();
		assertThat(result2.get()).isEqualTo(externalCloudEventType);
	}

	@Example
	void getMessageAliasType() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		String aliasType = "serializedType";
		Class<MappingEvent> internalEnvelopDataType = MappingEvent.class;
		sut.addSerializeTypeAlias(internalEnvelopDataType, aliasType);

		// when
		Optional<String> actual = sut.getMessageAliasType(internalEnvelopDataType);

		// then
		assertThat(actual).isPresent();
		assertThat(actual.get()).isEqualTo(aliasType);
	}

	@Example
	void getMessageType() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		String aliasType = "deserializedType";
		Class<MappingEvent> deserializedType = MappingEvent.class;
		sut.addDeserializeTypeAlias(aliasType, deserializedType);

		// when
		Optional<Class<? extends Message>> actual = sut.getMessageType(aliasType);

		// then
		assertThat(actual).isPresent();
		assertThat(actual.get()).isEqualTo(deserializedType);
	}

	@Example
	void getSerializeTypeAlias() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		String aliasType = "serializedType";
		Class<MappingEvent> internalEnvelopDataType = MappingEvent.class;
		sut.addSerializeTypeAlias(internalEnvelopDataType, aliasType);

		// when
		Optional<String> actual = sut.getSerializeTypeAlias(internalEnvelopDataType);

		// then
		assertThat(actual).isPresent();
		assertThat(actual.get()).isEqualTo(aliasType);
	}

	@Example
	void getSerializeTypeAliasEmpty() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		Class<MappingEvent> internalEnvelopDataType = MappingEvent.class;

		// when
		Optional<String> actual = sut.getSerializeTypeAlias(internalEnvelopDataType);

		// then
		assertThat(actual).isNotPresent();
	}

	@Example
	void getDeserializeTypeAlias() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		String aliasType = "deserializedType";
		Class<MappingEvent> deserializedType = MappingEvent.class;
		sut.addDeserializeTypeAlias(aliasType, deserializedType);

		// when
		Optional<Class<? extends Message>> actual = sut.getDeserializeTypeAlias(aliasType);

		// then
		assertThat(actual).isPresent();
		assertThat(actual.get()).isEqualTo(deserializedType);
	}

	@Example
	void getDeserializeTypeAliasEmpty() {
		// given
		CloudEventMessageTypeAliasMapper sut = new CloudEventMessageTypeAliasMapper();
		String externalEnvelopType = MappingEvent.class.getName();

		// when
		Optional<Class<? extends Message>> actual = sut.getDeserializeTypeAlias(externalEnvelopType);

		// then
		assertThat(actual).isNotPresent();
	}

	private static class MappingEvent implements Event {
		@Override
		public UUID getId() {
			return null;
		}

		@Override
		public OffsetDateTime getOccurrenceTime() {
			return null;
		}

		@Override
		public String getSourceId() {
			return null;
		}

		@Nullable
		@Override
		public Long getSourceVersion() {
			return null;
		}

		@Override
		public String getSourceType() {
			return null;
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

	private static class MappingObject {
	}
}
