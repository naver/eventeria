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

package com.navercorp.eventeria.messaging.fixture;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.distribution.Partitioned;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensionAppender;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensions;
import com.navercorp.eventeria.timer.contract.TimerMessage;

public class MessageFixtures extends DomainContextBase {
	@Provide
	public Arbitrary<TestMessage> testMessage() {
		return Arbitraries.ofSuppliers(() -> new TestMessage(Arbitraries.strings().alpha().sample()));
	}

	@Provide
	public Arbitrary<TestTimerMessage> testTimerMessage() {
		return Arbitraries.ofSuppliers(() -> new TestTimerMessage(Arbitraries.strings().alpha().sample()));
	}

	@Provide
	public Arbitrary<TestPartitioned> testPartitioned() {
		return Arbitraries.ofSuppliers(() -> new TestPartitioned(Arbitraries.strings().alpha().sample()));
	}

	@Getter
	@Setter
	@ToString
	public static class TestMessage implements Message {
		private String id;
		private OffsetDateTime occurrenceTime;
		private String name;

		public TestMessage() {
		}

		public TestMessage(String name) {
			this.id = UUID.randomUUID().toString();
			this.occurrenceTime = OffsetDateTime.now();
			this.name = name;
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public OffsetDateTime getOccurrenceTime() {
			return this.occurrenceTime;
		}

		@Nullable
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
		public Optional<String> getCorrelationId() {
			return Optional.empty();
		}

		@Override
		public Optional<String> getOperationId() {
			return Optional.empty();
		}
	}

	@Getter
	@Setter
	@ToString
	public static class TestTimerMessage implements Message, TimerMessage {
		private String name;

		public TestTimerMessage() {
		}

		public TestTimerMessage(String name) {
			this.name = name;
		}

		@Override
		public String getId() {
			return null;
		}

		@Override
		public OffsetDateTime getOccurrenceTime() {
			return null;
		}

		@Nullable
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
			return "com.navercorp.eventeria.order.Order";
		}

		@Override
		public Optional<String> getCorrelationId() {
			return Optional.empty();
		}

		@Override
		public Optional<String> getOperationId() {
			return Optional.empty();
		}

		@Override
		public Optional<Instant> timerTime() {
			return Optional.of(
				this.getOccurrenceTime().toInstant().plus(5, ChronoUnit.SECONDS)
			);
		}
	}

	@Getter
	@Setter
	@ToString
	public static class TestPartitioned implements Message, Partitioned,
		MessageExtensions, MessageExtensionAppender {

		private String id;
		private OffsetDateTime occurrenceTime;
		private String name;
		private Map<String, Object> extensions;

		public TestPartitioned() {
		}

		public TestPartitioned(String name) {
			this.id = UUID.randomUUID().toString();
			this.occurrenceTime = OffsetDateTime.now();
			this.name = name;
		}

		@Override
		public String getId() {
			return this.id;
		}

		@Override
		public OffsetDateTime getOccurrenceTime() {
			return this.occurrenceTime;
		}

		@Nullable
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
			return "com.navercorp.eventeria.order.Order";
		}

		@Override
		public Optional<String> getCorrelationId() {
			return Optional.empty();
		}

		@Override
		public Optional<String> getOperationId() {
			return Optional.empty();
		}

		@Override
		public String getPartitionKey() {
			return this.name;
		}

		@Nullable
		@Override
		public Object getExtension(String extensionName) {
			if (this.extensions == null) {
				this.extensions = new HashMap<>();
			}
			return this.extensions.get(extensionName.toLowerCase());
		}

		@Override
		public Set<String> getExtensionNames() {
			if (this.extensions == null) {
				this.extensions = new HashMap<>();
			}
			return this.extensions.keySet();
		}

		@Override
		public void appendExtension(String extensionName, @Nullable Object extensionValue) {
			if (this.extensions == null) {
				this.extensions = new HashMap<>();
			}
			this.extensions.put(extensionName.toLowerCase(), extensionValue);
		}
	}
}
