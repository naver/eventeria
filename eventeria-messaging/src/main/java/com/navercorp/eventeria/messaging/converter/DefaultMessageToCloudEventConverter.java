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

import static java.util.stream.Collectors.toSet;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventAttributes;
import io.cloudevents.CloudEventContext;
import io.cloudevents.CloudEventExtensions;
import io.cloudevents.SpecVersion;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.core.data.PojoCloudEventData;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.contract.serializer.MessageSerializer;

public class DefaultMessageToCloudEventConverter implements MessageToCloudEventConverter {
	private final CloudEventAttributesConverter cloudEventAttributesConverter;
	private final CloudEventExtensionsConverter cloudEventExtensionsConverter;
	private final MessageSerializer messageSerializer;

	public DefaultMessageToCloudEventConverter(
		CloudEventAttributesConverter cloudEventAttributesConverter,
		CloudEventExtensionsConverter cloudEventExtensionsConverter,
		MessageSerializer messageSerializer
	) {
		this.cloudEventAttributesConverter = cloudEventAttributesConverter;
		this.cloudEventExtensionsConverter = cloudEventExtensionsConverter;
		this.messageSerializer = messageSerializer;
	}

	@Override
	public CloudEvent convert(Message message) {
		if (message instanceof CloudEvent cloudEvent) {
			return cloudEvent;
		}

		CloudEventAttributes cloudEventAttributes = this.cloudEventAttributesConverter.convert(message);
		CloudEventExtensions cloudEventExtensions = this.cloudEventExtensionsConverter.convert(message);
		CloudEventContext cloudEventContext = this.toCloudEventContext(cloudEventAttributes, cloudEventExtensions);

		return CloudEventBuilder.fromContext(cloudEventContext)
			.withData(PojoCloudEventData.wrap(message, it -> this.messageSerializer.serialize(it, true)))
			.build();
	}

	@ParametersAreNonnullByDefault
	private CloudEventContext toCloudEventContext(
		CloudEventAttributes cloudEventAttributes,
		CloudEventExtensions cloudEventExtensions
	) {
		return new CloudEventContext() {
			@Override
			public SpecVersion getSpecVersion() {
				return cloudEventAttributes.getSpecVersion();
			}

			@Override
			public String getId() {
				return cloudEventAttributes.getId();
			}

			@Override
			public String getType() {
				return cloudEventAttributes.getType();
			}

			@Override
			public URI getSource() {
				return cloudEventAttributes.getSource();
			}

			@Nullable
			@Override
			public String getDataContentType() {
				return cloudEventAttributes.getDataContentType();
			}

			@Nullable
			@Override
			public URI getDataSchema() {
				return cloudEventAttributes.getDataSchema();
			}

			@Nullable
			@Override
			public String getSubject() {
				return cloudEventAttributes.getSubject();
			}

			@Nullable
			@Override
			public OffsetDateTime getTime() {
				return cloudEventAttributes.getTime();
			}

			@Nullable
			@Override
			public Object getAttribute(String attributeName) throws IllegalArgumentException {
				return cloudEventAttributes.getAttribute(attributeName);
			}

			@Nullable
			@Override
			public Object getExtension(String extensionName) {
				return cloudEventExtensions.getExtension(extensionName);
			}

			@Override
			public Set<String> getExtensionNames() {
				return cloudEventExtensions.getExtensionNames().stream()
					.filter(it -> getExtension(it) != null)
					.collect(toSet());
			}
		};
	}
}
