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

import java.util.Optional;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.extension.CloudEventTypeAliasExtension;
import com.navercorp.eventeria.messaging.typealias.MessageDeserializeTypeAliasMapper;
import com.navercorp.eventeria.messaging.typealias.MessageTypeAliasNotFoundException;

public class MessageDeserializeTypeAliasConverter implements MessageDeserializeTypeConverter {
	private final MessageDeserializeTypeAliasMapper messageDeserializeTypeAliasMapper;

	public MessageDeserializeTypeAliasConverter(MessageDeserializeTypeAliasMapper messageDeserializeTypeAliasMapper) {
		this.messageDeserializeTypeAliasMapper = messageDeserializeTypeAliasMapper;
	}

	/**
	 * Find target class type from {@link CloudEventTypeAliasExtension} first,
	 * if not exists, find next from {@link CloudEvent#getType()}.
	 *
	 * @param cloudEvent cloudevent to find deserializing class type
	 * @return the deserializing class type
	 */
	@Override
	public Class<? extends Message> convert(CloudEvent cloudEvent) {
		String type = getTypeString(cloudEvent);

		Optional<Class<? extends Message>> deserializeType =
			this.messageDeserializeTypeAliasMapper.getDeserializeTypeAlias(type);
		if (deserializeType.isPresent()) {
			return deserializeType.get();
		}

		return this.findClass(type);
	}

	private String getTypeString(CloudEvent cloudEvent) {
		CloudEventTypeAliasExtension typeAliasExtension = CloudEventTypeAliasExtension.parseExtension(cloudEvent);
		if (typeAliasExtension != null && typeAliasExtension.getTypeAlias() != null) {
			return typeAliasExtension.getTypeAlias();
		}

		return cloudEvent.getType();
	}

	@SuppressWarnings("unchecked")
	private Class<? extends Message> findClass(String className) {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> messageType;
		try {
			messageType = Class.forName(className, true, loader);
		} catch (Exception ex) {
			throw new MessageTypeAliasNotFoundException(
				className,
				"Can not find message type. Add mapping type information. typeName: " + className,
				ex
			);
		}

		if (!Message.class.isAssignableFrom(messageType)) {
			String messageTypeName = Message.class.getName();
			throw new MessageTypeAliasNotFoundException(
				className,
				"Found message type can not assignable \"" + messageTypeName + "\". "
					+ "Please implements \"" + Message.class.getName() + "\" "
					+ "or add mapping type information. "
					+ "typeName: " + className
			);
		}

		return (Class<? extends Message>)messageType;
	}
}
