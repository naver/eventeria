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

package com.navercorp.eventeria.messaging.contract.command;

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

/**
 * a abstract class to support implementation of {@link Command}
 */
public abstract class AbstractCommand implements Command, MessageExtensions, MessageExtensionAppender, Partitioned {
	private String id;
	private String sourceId;
	private Long sourceVersion;
	private URI source;
	private URI dataSchema;
	private String subject;
	private String partitionKey;
	private String correlationId;
	private String operationId;
	private OffsetDateTime occurrenceTime;
	private Map<String, Object> extensions = new HashMap<>();

	protected AbstractCommand() {
		this.id = UUID.randomUUID().toString();
	}

	// There is no sourceId in the Command which raises to target that is not created an Aggregate.
	protected AbstractCommand(@Nullable String sourceId) {
		this.id = UUID.randomUUID().toString();
		this.sourceId = sourceId;
		this.occurrenceTime = OffsetDateTime.now();
	}

	protected AbstractCommand(@Nullable String sourceId, @Nullable Long sourceVersion) {
		this.id = UUID.randomUUID().toString();
		this.sourceId = sourceId;
		this.sourceVersion = sourceVersion;
		this.occurrenceTime = OffsetDateTime.now();
	}

	protected AbstractCommand(String sourceId, @Nullable Long sourceVersion, Instant occurrenceTime) {
		this(sourceId, sourceVersion, OffsetDateTime.ofInstant(occurrenceTime, ZoneId.systemDefault()));
	}

	protected AbstractCommand(String sourceId, @Nullable Long sourceVersion, OffsetDateTime occurrenceTime) {
		guardHeaderProperties(sourceId, sourceVersion, occurrenceTime);
		this.id = UUID.randomUUID().toString();
		this.sourceId = sourceId;
		this.sourceVersion = sourceVersion;
		this.occurrenceTime = occurrenceTime;
	}

	private static void guardHeaderProperties(Object sourceId, Long version, OffsetDateTime occurrenceTime) {
		Objects.requireNonNull(sourceId, "The parameter 'sourceId' cannot be null.");
		Objects.requireNonNull(occurrenceTime, "The parameter 'occurrenceTime' cannot be null.");
		if (version != null && version < 0L) {
			throw new IllegalArgumentException(
				"The parameter 'sourceVersion' must be greater or equals than 0. version: " + version);
		}
	}

	@Override
	public String getId() {
		return this.id;
	}

	protected void setId(String id) {
		this.id = id;
	}

	@Override
	public OffsetDateTime getOccurrenceTime() {
		return this.occurrenceTime;
	}

	protected void setOccurrenceTime(OffsetDateTime occurrenceTime) {
		this.occurrenceTime = occurrenceTime;
	}

	// There is no sourceId in the Command which raises to target that is not created an Aggregate.
	@Nullable
	@Override
	public String getSourceId() {
		return this.sourceId;
	}

	protected void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	@Nullable
	public Long getSourceVersion() {
		return this.sourceVersion;
	}

	protected void setSourceVersion(@Nullable Long sourceVersion) {
		this.sourceVersion = sourceVersion;
	}

	@Override
	public URI getSource() {
		if (this.source == null) {
			return Command.super.getSource();
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
	public Optional<String> getCorrelationId() {
		return Optional.ofNullable(this.correlationId);
	}

	protected void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	@Override
	public Optional<String> getOperationId() {
		return Optional.ofNullable(this.operationId);
	}

	protected void setOperationId(String operationId) {
		this.operationId = operationId;
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

	protected Map<String, Object> getExtensions() {
		if (this.extensions == null) {
			this.extensions = new HashMap<>();
		}
		return this.extensions;
	}

	protected void setExtensions(Map<String, Object> extensions) {
		extensions.forEach(this::appendExtension);
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

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		AbstractCommand that = (AbstractCommand)obj;
		return Objects.equals(this.id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.id);
	}

	@Override
	public String toString() {
		return "AbstractCommand{"
			+ "id=" + id
			+ ", sourceId='" + sourceId + '\''
			+ ", sourceVersion=" + sourceVersion
			+ ", source=" + source
			+ ", dataSchema=" + dataSchema
			+ ", subject='" + subject + '\''
			+ ", partitionKey='" + partitionKey + '\''
			+ ", correlationId=" + correlationId
			+ ", operationId='" + operationId + '\''
			+ ", occurrenceTime=" + occurrenceTime
			+ ", extensions=" + extensions
			+ '}';
	}
}
