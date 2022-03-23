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
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.cloudevents.CloudEventExtensions;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.extension.EmptyCloudEventExtensions;
import com.navercorp.eventeria.messaging.extension.CloudEventTypeAliasExtension;
import com.navercorp.eventeria.messaging.typealias.MessageSerializeTypeAliasMapper;

@ParametersAreNonnullByDefault
public final class CloudEventTypeAliasExtensionsConverter implements CloudEventExtensionsConverter {
	private final MessageSerializeTypeAliasMapper messageSerializeTypeAliasMapper;

	public CloudEventTypeAliasExtensionsConverter(MessageSerializeTypeAliasMapper messageSerializeTypeAliasMapper) {
		this.messageSerializeTypeAliasMapper = messageSerializeTypeAliasMapper;
	}

	@Override
	public CloudEventExtensions convert(Message message) {
		Optional<String> aliasType = this.messageSerializeTypeAliasMapper.getSerializeTypeAlias(message.getClass());
		if (!aliasType.isPresent()) {
			return EmptyCloudEventExtensions.INSTANCE;
		}

		return new CloudEventExtensions() {
			@Nullable
			@Override
			public Object getExtension(String extensionName) {
				if (CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION.equals(extensionName)) {
					return aliasType.get();
				}

				return null;
			}

			@Override
			public Set<String> getExtensionNames() {
				return CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION_KEYS;
			}
		};
	}
}
