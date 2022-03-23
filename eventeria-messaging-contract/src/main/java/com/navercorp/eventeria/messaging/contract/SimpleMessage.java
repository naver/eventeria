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

package com.navercorp.eventeria.messaging.contract;

import java.net.URI;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Nullable;

import com.navercorp.eventeria.messaging.contract.distribution.Partitioned;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensionAppender;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensions;

public class SimpleMessage implements Message, Partitioned, MessageExtensions, MessageExtensionAppender {
	private UUID id;
	private OffsetDateTime occurrenceTime;
	private String sourceId;
	private Long sourceVersion;
	private String sourceType;
	private URI source;
	private URI dataSchema;
	private String subject;
	private String partitionKey;
	private UUID correlationId;
	private String operationId;
	private Map<String, Object> payload = new HashMap<>();
	private Map<String, Object> extensions = new HashMap<>();

	SimpleMessage() {
	}

	protected SimpleMessage(
		UUID id,
		OffsetDateTime occurrenceTime,
		String sourceId,
		Long sourceVersion,
		String sourceType,
		URI dataSchema,
		String subject,
		String partitionKey,
		UUID correlationId,
		String operationId,
		Map<String, Object> payload,
		Map<String, Object> extensions
	) {
		this.id = id;
		this.occurrenceTime = occurrenceTime;
		this.sourceId = sourceId;
		this.sourceVersion = sourceVersion;
		this.sourceType = sourceType;
		this.dataSchema = dataSchema;
		this.subject = subject;
		this.partitionKey = partitionKey;
		this.correlationId = correlationId;
		this.operationId = operationId;
		this.payload = payload;
		this.extensions = extensions;
	}

	public static SimpleMessageBuilder builder() {
		return new SimpleMessageBuilder();
	}

	@Override
	public UUID getId() {
		return this.id;
	}

	protected void setId(UUID id) {
		this.id = id;
	}

	@Override
	public OffsetDateTime getOccurrenceTime() {
		return this.occurrenceTime;
	}

	protected void setOccurrenceTime(OffsetDateTime occurrenceTime) {
		this.occurrenceTime = occurrenceTime;
	}

	@Override
	public String getSourceId() {
		return this.sourceId;
	}

	protected void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	@Nullable
	@Override
	public Long getSourceVersion() {
		return this.sourceVersion;
	}

	protected void setSourceVersion(Long sourceVersion) {
		this.sourceVersion = sourceVersion;
	}

	@Override
	public String getSourceType() {
		return this.sourceType;
	}

	protected void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	@Override
	public URI getSource() {
		if (this.source == null) {
			return Message.super.getSource();
		}
		return this.source;
	}

	protected void setSource(URI source) {
		this.source = source;
	}

	@Override
	public Optional<URI> getDataSchema() {
		return Optional.ofNullable(this.dataSchema);
	}

	protected void setDataSchema(URI dataSchema) {
		this.dataSchema = dataSchema;
	}

	@Override
	public Optional<String> getSubject() {
		return Optional.ofNullable(this.subject);
	}

	protected void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public Optional<UUID> getCorrelationId() {
		return Optional.ofNullable(this.correlationId);
	}

	protected void setCorrelationId(UUID correlationId) {
		this.correlationId = correlationId;
	}

	@Override
	public Optional<String> getOperationId() {
		return Optional.ofNullable(this.operationId);
	}

	protected void setOperationId(String operationId) {
		this.operationId = operationId;
	}

	public Map<String, Object> getPayload() {
		if (this.payload == null) {
			this.payload = new HashMap<>();
		}

		return Collections.unmodifiableMap(this.payload);
	}

	protected void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}

	protected void setPayload(String fieldName, Object value) {
		if (this.payload == null) {
			this.payload = new HashMap<>();
		}

		this.payload.put(fieldName, value);
	}

	public Object get(String fieldName) {
		return this.getPayload().get(fieldName);
	}

	public <T> T get(String fieldName, Class<T> valueType) {
		return valueType.cast(this.getPayload().get(fieldName));
	}

	@Override
	public String getPartitionKey() {
		if (this.partitionKey == null) {
			return this.getSource().toString();
		}

		return this.partitionKey;
	}

	protected void setPartitionKey(String partitionKey) {
		this.partitionKey = partitionKey;
	}

	/**
	 * CloudEvents attribute names MUST consist of lower-case letters ('a' to 'z') or digits ('0' to '9')
	 * from the ASCII character set.
	 * Attribute names SHOULD be descriptive and terse and SHOULD NOT exceed 20 characters in length.
	 *
	 * @see <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md#attribute-naming-convention">attribute-naming-convention</a>
	 */
	@Nullable
	@Override
	public Object getExtension(String extensionName) {
		if (this.extensions == null) {
			return null;
		}

		return this.extensions.get(extensionName.toLowerCase());
	}

	@Override
	public Set<String> getExtensionNames() {
		if (this.extensions == null) {
			return Collections.emptySet();
		}

		return this.extensions.keySet();
	}

	/**
	 * CloudEvents attribute names MUST consist of lower-case letters ('a' to 'z') or digits ('0' to '9')
	 * from the ASCII character set.
	 * Attribute names SHOULD be descriptive and terse and SHOULD NOT exceed 20 characters in length.
	 *
	 * @see <a href="https://github.com/cloudevents/spec/blob/v1.0.1/spec.md#attribute-naming-convention">attribute-naming-convention</a>
	 */
	@Override
	public void appendExtension(String extensionName, @Nullable Object extensionValue) {
		if (this.extensions == null) {
			this.extensions = new HashMap<>();
		}

		this.extensions.put(extensionName.toLowerCase(), extensionValue);
	}

	protected void setExtensions(Map<String, Object> extensions) {
		extensions.forEach(this::appendExtension);
	}

	protected Map<String, Object> getExtensions() {
		if (this.extensions == null) {
			this.extensions = new HashMap<>();
		}
		return this.extensions;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		SimpleMessage that = (SimpleMessage)obj;
		return Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public String toString() {
		return "SimpleMessage{"
			+ "id=" + id
			+ ", occurrenceTime=" + occurrenceTime
			+ ", sourceId='" + sourceId + '\''
			+ ", sourceVersion=" + sourceVersion
			+ ", sourceType='" + sourceType + '\''
			+ ", source=" + source
			+ ", dataSchema=" + dataSchema
			+ ", subject='" + subject + '\''
			+ ", partitionKey='" + partitionKey + '\''
			+ ", correlationId=" + correlationId
			+ ", operationId='" + operationId + '\''
			+ ", payload=" + payload
			+ ", extensions=" + extensions
			+ '}';
	}

	public static class SimpleMessageBuilder {
		private UUID id;
		private OffsetDateTime occurrenceTime;
		private String sourceId;
		private Long sourceVersion;
		private String sourceType;
		private URI dataSchema;
		private String subject;
		private String partitionKey;
		private UUID correlationId;
		private String operationId;
		private Map<String, Object> payload = new HashMap<>();
		private Map<String, Object> extensions = new HashMap<>();

		public SimpleMessageBuilder() {
		}

		public SimpleMessageBuilder id(UUID id) {
			this.id = id;
			return this;
		}

		public SimpleMessageBuilder occurrenceTime(Instant occurrenceTime) {
			return this.occurrenceTime(OffsetDateTime.ofInstant(occurrenceTime, ZoneId.systemDefault()));
		}

		public SimpleMessageBuilder occurrenceTime(OffsetDateTime occurrenceTime) {
			this.occurrenceTime = occurrenceTime;
			return this;
		}

		public SimpleMessageBuilder sourceId(String sourceId) {
			this.sourceId = sourceId;
			return this;
		}

		public SimpleMessageBuilder sourceVersion(Long sourceVersion) {
			this.sourceVersion = sourceVersion;
			return this;
		}

		public SimpleMessageBuilder sourceType(String sourceType) {
			this.sourceType = sourceType;
			return this;
		}

		public SimpleMessageBuilder dataSchema(URI dataSchema) {
			this.dataSchema = dataSchema;
			return this;
		}

		public SimpleMessageBuilder subject(String subject) {
			this.subject = subject;
			return this;
		}

		public SimpleMessageBuilder partitionKey(String partitionKey) {
			this.partitionKey = partitionKey;
			return this;
		}

		public SimpleMessageBuilder correlationId(UUID correlationId) {
			this.correlationId = correlationId;
			return this;
		}

		public SimpleMessageBuilder operationId(String operationId) {
			this.operationId = operationId;
			return this;
		}

		public SimpleMessageBuilder payload(Map<String, Object> payload) {
			this.payload = new HashMap<>(payload);
			return this;
		}

		public SimpleMessageBuilder add(String fieldName, Object value) {
			if (this.payload == null) {
				this.payload = new HashMap<>();
			}
			this.payload.put(fieldName, value);
			return this;
		}

		public SimpleMessageBuilder extensions(Map<String, Object> extensions) {
			this.extensions = new HashMap<>(extensions);
			return this;
		}

		public SimpleMessage build() {
			if (this.id == null) {
				this.id = UUID.randomUUID();
			}
			if (this.occurrenceTime == null) {
				this.occurrenceTime = OffsetDateTime.now();
			}
			if (this.payload == null) {
				this.payload = new HashMap<>();
			}
			if (this.extensions == null) {
				this.extensions = new HashMap<>();
			}

			return new SimpleMessage(
				this.id,
				this.occurrenceTime,
				this.sourceId,
				this.sourceVersion,
				this.sourceType,
				this.dataSchema,
				this.subject,
				this.partitionKey,
				this.correlationId,
				this.operationId,
				this.payload,
				this.extensions
			);
		}
	}
}
