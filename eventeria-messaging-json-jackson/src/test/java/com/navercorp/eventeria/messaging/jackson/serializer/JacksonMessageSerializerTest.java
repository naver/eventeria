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

package com.navercorp.eventeria.messaging.jackson.serializer;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.data.TemporalUnitWithinOffset;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.eventeria.messaging.jackson.MessageObjectMappers;
import com.navercorp.eventeria.messaging.jackson.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.jackson.fixture.EventFixtures.TestDomainEvent;

class JacksonMessageSerializerTest {
	private final ObjectMapper objectMapper = MessageObjectMappers.getMessageObjectMapper();
	private final JacksonMessageSerializer sut = new JacksonMessageSerializer(objectMapper);

	@Example
	@Domain(EventFixtures.class)
	void serialize(@ForAll TestDomainEvent testDomainEvent) throws IOException {
		// when
		byte[] actual = this.sut.serialize(testDomainEvent);

		// then
		Map<String, Object> jsonNode = this.objectMapper.readValue(actual, new TypeReference<Map<String, Object>>() {
		});
		assertThat(jsonNode.get("name")).isEqualTo(testDomainEvent.getName());
		assertThat(jsonNode.get("id")).isEqualTo(testDomainEvent.getId().toString());
		assertThat(
			jsonNode.get("sourceVersion") != null ? jsonNode.get("sourceVersion").toString() : null
		).isEqualTo(
			testDomainEvent.getSourceVersion() != null ? testDomainEvent.getSourceVersion().toString() : null);
		assertThat(jsonNode.get("correlationId"))
			.isEqualTo(testDomainEvent.getCorrelationId().map(UUID::toString).orElse(null));
		assertThat(jsonNode.get("operationId")).isEqualTo(testDomainEvent.getOperationId().orElse(null));
		assertThat(jsonNode.get("sourceType")).isEqualTo(testDomainEvent.getSourceType());
		assertThat(jsonNode.get("source")).isEqualTo(testDomainEvent.getSource().toString());
		assertThat(jsonNode.get("dataSchema"))
			.isEqualTo(testDomainEvent.getDataSchema().map(URI::toString).orElse(null));
		assertThat(jsonNode.get("subject")).isEqualTo(testDomainEvent.getSubject().orElse(null));
		assertThat(jsonNode.get("partitionKey")).isEqualTo(testDomainEvent.getPartitionKey());

		OffsetDateTime occurrenceTime = OffsetDateTime.parse(jsonNode.get("occurrenceTime").toString());
		assertThat(occurrenceTime)
			.isCloseTo(testDomainEvent.getOccurrenceTime(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(jsonNode.containsKey("extensions")).isTrue();
		assertThat(jsonNode.containsKey("extensionNames")).isFalse();
	}

	@Example
	@Domain(EventFixtures.class)
	void serializeExceptExtensions(@ForAll TestDomainEvent testDomainEvent) throws IOException {
		// when
		byte[] actual = this.sut.serialize(testDomainEvent, true);

		// then
		Map<String, Object> jsonNode = this.objectMapper.readValue(actual, new TypeReference<Map<String, Object>>() {
		});
		assertThat(jsonNode.get("name")).isEqualTo(testDomainEvent.getName());
		assertThat(jsonNode.get("id")).isEqualTo(testDomainEvent.getId().toString());
		assertThat(
			jsonNode.get("sourceVersion") != null ? jsonNode.get("sourceVersion").toString() : null
		).isEqualTo(
			testDomainEvent.getSourceVersion() != null ? testDomainEvent.getSourceVersion().toString() : null);
		assertThat(jsonNode.get("correlationId"))
			.isEqualTo(testDomainEvent.getCorrelationId().map(UUID::toString).orElse(null));
		assertThat(jsonNode.get("operationId")).isEqualTo(testDomainEvent.getOperationId().orElse(null));
		assertThat(jsonNode.get("sourceType")).isEqualTo(testDomainEvent.getSourceType());
		assertThat(jsonNode.get("source")).isEqualTo(testDomainEvent.getSource().toString());
		assertThat(jsonNode.get("dataSchema"))
			.isEqualTo(testDomainEvent.getDataSchema().map(URI::toString).orElse(null));
		assertThat(jsonNode.get("subject")).isEqualTo(testDomainEvent.getSubject().orElse(null));
		assertThat(jsonNode.get("partitionKey")).isEqualTo(testDomainEvent.getPartitionKey());

		OffsetDateTime occurrenceTime = OffsetDateTime.parse(jsonNode.get("occurrenceTime").toString());
		assertThat(occurrenceTime)
			.isCloseTo(testDomainEvent.getOccurrenceTime(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(jsonNode.containsKey("extensions")).isFalse();
		assertThat(jsonNode.containsKey("extensionNames")).isFalse();
	}

	@Example
	@Domain(EventFixtures.class)
	void deserialize(@ForAll TestDomainEvent testDomainEvent) {
		// given
		byte[] serialized = this.sut.serialize(testDomainEvent);

		// when
		TestDomainEvent actual = this.sut.deserialize(serialized, TestDomainEvent.class);

		// then
		assertThat(actual.getName()).isEqualTo(testDomainEvent.getName());
		assertThat(actual.getId()).isEqualTo(testDomainEvent.getId());
		assertThat(actual.getSourceVersion()).isEqualTo(testDomainEvent.getSourceVersion());
		assertThat(actual.getCorrelationId()).isEqualTo(testDomainEvent.getCorrelationId());
		assertThat(actual.getOperationId()).isEqualTo(testDomainEvent.getOperationId());
		assertThat(actual.getSourceType()).isEqualTo(testDomainEvent.getSourceType());
		assertThat(actual.getSource()).isEqualTo(testDomainEvent.getSource());
		assertThat(actual.getDataSchema()).isEqualTo(testDomainEvent.getDataSchema());
		assertThat(actual.getSubject()).isEqualTo(testDomainEvent.getSubject());
		assertThat(actual.getPartitionKey()).isEqualTo(testDomainEvent.getPartitionKey());
		assertThat(actual.getOccurrenceTime())
			.isCloseTo(testDomainEvent.getOccurrenceTime(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(actual.getExtensionNames()).hasSize(testDomainEvent.getExtensionNames().size());
		actual.getExtensionNames().forEach(it ->
			assertThat(actual.getExtension(it)).isEqualTo(testDomainEvent.getExtension(it))
		);
	}

	@Example
	@Domain(EventFixtures.class)
	void deserializeExceptExtensions(@ForAll TestDomainEvent testDomainEvent) {
		// given
		byte[] serialized = this.sut.serialize(testDomainEvent, true);

		// when
		TestDomainEvent actual = this.sut.deserialize(serialized, TestDomainEvent.class);

		// then
		assertThat(actual.getName()).isEqualTo(testDomainEvent.getName());
		assertThat(actual.getId()).isEqualTo(testDomainEvent.getId());
		assertThat(actual.getSourceVersion()).isEqualTo(testDomainEvent.getSourceVersion());
		assertThat(actual.getCorrelationId()).isEqualTo(testDomainEvent.getCorrelationId());
		assertThat(actual.getOperationId()).isEqualTo(testDomainEvent.getOperationId());
		assertThat(actual.getSourceType()).isEqualTo(testDomainEvent.getSourceType());
		assertThat(actual.getSource()).isEqualTo(testDomainEvent.getSource());
		assertThat(actual.getDataSchema()).isEqualTo(testDomainEvent.getDataSchema());
		assertThat(actual.getSubject()).isEqualTo(testDomainEvent.getSubject());
		assertThat(actual.getPartitionKey()).isEqualTo(testDomainEvent.getPartitionKey());
		assertThat(actual.getOccurrenceTime())
			.isCloseTo(testDomainEvent.getOccurrenceTime(), new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS));
		assertThat(actual.getExtensionNames()).isEmpty();
	}
}
