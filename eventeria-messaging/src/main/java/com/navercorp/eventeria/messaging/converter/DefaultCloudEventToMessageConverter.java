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

import javax.annotation.Nullable;

import io.cloudevents.CloudEvent;
import io.cloudevents.CloudEventData;
import io.cloudevents.core.data.PojoCloudEventData;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensionAppender;
import com.navercorp.eventeria.messaging.contract.serializer.MessageDeserializer;
import com.navercorp.eventeria.messaging.converter.fallback.DeserializeMessageFailureFallback;
import com.navercorp.eventeria.messaging.typealias.MessageDeserializeTypeAliasMapper;

public class DefaultCloudEventToMessageConverter implements CloudEventToMessageConverter {
	private final MessageDeserializeTypeConverter messageDeserializeTypeConverter;
	private final MessageDeserializer messageDeserializer;

	@Nullable
	private final DeserializeMessageFailureFallback deserializeMessageFailureFallback;

	public DefaultCloudEventToMessageConverter(
		MessageDeserializeTypeAliasMapper messageDeserializeTypeAliasMapper,
		MessageDeserializer messageDeserializer
	) {
		this(
			new MessageDeserializeTypeAliasConverter(messageDeserializeTypeAliasMapper),
			messageDeserializer,
			null
		);
	}

	public DefaultCloudEventToMessageConverter(
		MessageDeserializeTypeConverter messageDeserializeTypeConverter,
		MessageDeserializer messageDeserializer
	) {
		this(messageDeserializeTypeConverter, messageDeserializer, null);
	}

	public DefaultCloudEventToMessageConverter(
		MessageDeserializeTypeConverter messageDeserializeTypeConverter,
		MessageDeserializer messageDeserializer,
		@Nullable DeserializeMessageFailureFallback deserializeMessageFailureFallback
	) {
		this.messageDeserializeTypeConverter = messageDeserializeTypeConverter;
		this.messageDeserializer = messageDeserializer;
		this.deserializeMessageFailureFallback = deserializeMessageFailureFallback;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Message convert(CloudEvent cloudEvent) {
		CloudEventData cloudEventData = cloudEvent.getData();
		if (cloudEventData == null) {
			throw new RuntimeException("CloudEvent data can not be null to convert Message. cloudEvent: " + cloudEvent);
		}

		if (cloudEventData instanceof PojoCloudEventData) {
			return (Message)((PojoCloudEventData)cloudEventData).getValue();
		}

		Message message = this.deserialize(cloudEvent);
		if (message instanceof MessageExtensionAppender) {
			MessageExtensionAppender appender = (MessageExtensionAppender)message;
			for (String extensionName : cloudEvent.getExtensionNames()) {
				appender.appendExtension(extensionName, cloudEvent.getExtension(extensionName));
			}
		}
		return message;
	}

	private Message deserialize(CloudEvent cloudEvent) {
		Class<? extends Message> deserializeType = this.messageDeserializeTypeConverter.convert(cloudEvent);
		try {
			return this.messageDeserializer.deserialize(cloudEvent.getData().toBytes(), deserializeType);
		} catch (Throwable throwable) {
			if (this.deserializeMessageFailureFallback != null) {
				return this.deserializeMessageFailureFallback.fallback(cloudEvent, throwable);
			} else {
				throw throwable;
			}
		}
	}
}
