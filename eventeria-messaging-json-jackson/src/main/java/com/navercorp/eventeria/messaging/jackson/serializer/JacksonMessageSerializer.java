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

import com.fasterxml.jackson.databind.ObjectMapper;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensions;
import com.navercorp.eventeria.messaging.contract.serializer.MessageSerializerDeserializer;
import com.navercorp.eventeria.messaging.exception.MessageDeserializationException;
import com.navercorp.eventeria.messaging.exception.MessageSerializationException;
import com.navercorp.eventeria.messaging.jackson.MessageObjectMappers;
import com.navercorp.eventeria.messaging.jackson.mixin.MessageExtensionIgnoreMixin;
import com.navercorp.eventeria.messaging.jackson.mixin.MessageExtensionIncludeMixin;

public final class JacksonMessageSerializer implements MessageSerializerDeserializer {
	private final ObjectMapper objectMapper;
	private final ObjectMapper objectMapperIgnoreExtension;

	public JacksonMessageSerializer() {
		this(MessageObjectMappers.getMessageObjectMapper());
	}

	public JacksonMessageSerializer(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper
			.copy()
			.addMixIn(MessageExtensions.class, MessageExtensionIncludeMixin.class);
		this.objectMapperIgnoreExtension = objectMapper
			.copy()
			.addMixIn(MessageExtensions.class, MessageExtensionIgnoreMixin.class);
	}

	@Override
	public byte[] serialize(Message message, boolean exceptExtensions) {
		try {
			if (exceptExtensions) {
				return this.objectMapperIgnoreExtension.writeValueAsBytes(message);
			} else {
				return this.objectMapper.writeValueAsBytes(message);
			}
		} catch (Throwable ex) {
			throw new MessageSerializationException("Serialize message is failed.", ex);
		}
	}

	@Override
	public <M extends Message> M deserialize(byte[] messageValue, Class<M> type) {
		try {
			return this.objectMapper.readValue(messageValue, type);
		} catch (Throwable ex) {
			throw new MessageDeserializationException("Deserialize message is failed.", ex);
		}
	}
}
