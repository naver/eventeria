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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.navercorp.eventeria.messaging.contract.Message;

/**
 * A default implementation of serializing/deserializing mapper between typealias extensions and actual types.
 */
public class CloudEventMessageTypeAliasMapper
	implements MessageSerializeTypeAliasMapper, MessageDeserializeTypeAliasMapper {
	private final Map<Class<? extends Message>, String> serializeTypeAliasMap = new ConcurrentHashMap<>();
	private final Map<String, Class<? extends Message>> deserializeTypeAliasMap = new ConcurrentHashMap<>();

	public void addSerializeTypeAlias(Class<? extends Message> messageType, String aliasType) {
		this.serializeTypeAliasMap.put(messageType, aliasType);
	}

	public void addDeserializeTypeAlias(String aliasType, Class<? extends Message> messageType) {
		this.deserializeTypeAliasMap.put(aliasType, messageType);
	}

	public void addCompatibleTypeAlias(Class<? extends Message> messageType, String aliasType) {
		this.addSerializeTypeAlias(messageType, aliasType);
		this.addDeserializeTypeAlias(aliasType, messageType);
	}

	public Optional<String> getMessageAliasType(Class<? extends Message> messageType) {
		return Optional.ofNullable(this.serializeTypeAliasMap.get(messageType));
	}

	public Optional<Class<? extends Message>> getMessageType(String aliasType) {
		return Optional.ofNullable(this.deserializeTypeAliasMap.get(aliasType));
	}

	@Override
	public Optional<String> getSerializeTypeAlias(Class<? extends Message> messageType) {
		return this.getMessageAliasType(messageType);
	}

	@Override
	public Optional<Class<? extends Message>> getDeserializeTypeAlias(String messageType) {
		return this.getMessageType(messageType);
	}
}
