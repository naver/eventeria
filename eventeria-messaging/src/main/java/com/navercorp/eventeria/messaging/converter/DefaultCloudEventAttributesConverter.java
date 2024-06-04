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

import java.net.URI;
import java.time.OffsetDateTime;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.cloudevents.CloudEventAttributes;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.v03.CloudEventV03;
import io.cloudevents.core.v1.CloudEventV1;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventAttributesConverter;

@ParametersAreNonnullByDefault
public class DefaultCloudEventAttributesConverter implements CloudEventAttributesConverter {
	private final SpecVersion defaultSpecVersion;
	private final String defaultDataContentType;

	public DefaultCloudEventAttributesConverter() {
		this(SpecVersion.V1, "application/json");
	}

	public DefaultCloudEventAttributesConverter(
		SpecVersion defaultSpecVersion,
		String defaultDataContentType
	) {
		this.defaultSpecVersion = defaultSpecVersion;
		this.defaultDataContentType = defaultDataContentType;
	}

	@Override
	public CloudEventAttributes convert(Message message) {
		if (message instanceof CloudEventAttributes cloudEventAttributes) {
			return cloudEventAttributes;
		}

		return new CloudEventAttributes() {
			@Override
			public SpecVersion getSpecVersion() {
				return defaultSpecVersion;
			}

			@Override
			public String getId() {
				return message.getId();
			}

			@Override
			public String getType() {
				return message.getClass().getName();
			}

			@Override
			public URI getSource() {
				return message.getSource();
			}

			@Nullable
			@Override
			public String getDataContentType() {
				return defaultDataContentType;
			}

			@Nullable
			@Override
			public URI getDataSchema() {
				return message.getDataSchema().orElse(null);
			}

			@Nullable
			@Override
			public String getSubject() {
				return message.getSubject().orElse(null);
			}

			@Nullable
			@Override
			public OffsetDateTime getTime() {
				return message.getOccurrenceTime();
			}

			@Nullable
			@Override
			public Object getAttribute(String attributeName) throws IllegalArgumentException {
				SpecVersion specVersion = getSpecVersion();
				if (specVersion == SpecVersion.V03) {
					return this.getAttributeV03(attributeName);
				} else if (specVersion == SpecVersion.V1) {
					return this.getAttributeV1(attributeName);
				}

				throw new IllegalArgumentException(
					"This spec version doesn't defined yet. specVersion: " + specVersion
				);
			}

			@Nullable
			private Object getAttributeV03(String attributeName) {
				return switch (attributeName) {
					case CloudEventV03.SPECVERSION -> getSpecVersion();
					case CloudEventV03.ID -> getId();
					case CloudEventV03.SOURCE -> getSource();
					case CloudEventV03.TYPE -> getType();
					case CloudEventV03.DATACONTENTTYPE -> getDataContentType();
					case CloudEventV03.SCHEMAURL -> getDataSchema();
					case CloudEventV03.SUBJECT -> getSubject();
					case CloudEventV03.TIME -> getTime();
					case CloudEventV03.DATACONTENTENCODING ->
						// We don't save datacontentencoding, but the attribute name is valid,
						// hence we just return always null
						null;
					default -> throw new IllegalArgumentException(
						"Spec version v0.3 doesn't have attribute named " + attributeName);
				};

			}

			private Object getAttributeV1(String attributeName) {
				return switch (attributeName) {
					case CloudEventV1.SPECVERSION -> getSpecVersion();
					case CloudEventV1.ID -> getId();
					case CloudEventV1.SOURCE -> getSource();
					case CloudEventV1.TYPE -> getType();
					case CloudEventV1.DATACONTENTTYPE -> getDataContentType();
					case CloudEventV1.DATASCHEMA -> getDataSchema();
					case CloudEventV1.SUBJECT -> getSubject();
					case CloudEventV1.TIME -> getTime();
					default -> throw new IllegalArgumentException(
						"Spec version v1 doesn't have attribute named " + attributeName);
				};

			}
		};
	}
}
