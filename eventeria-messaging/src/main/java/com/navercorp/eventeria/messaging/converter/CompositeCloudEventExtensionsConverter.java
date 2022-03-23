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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.cloudevents.CloudEventExtensions;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

@ParametersAreNonnullByDefault
public final class CompositeCloudEventExtensionsConverter extends DefaultCloudEventExtensionsConverter {
	private final List<CloudEventExtensionsConverter> converters;

	public CompositeCloudEventExtensionsConverter() {
		this(
			new CloudEventTypeAliasExtensionsConverter(new CloudEventMessageTypeAliasMapper()),
			new MessageCategoryExtensionsConverter(),
			new PartitionKeyExtensionsConverter()
		);
	}

	public CompositeCloudEventExtensionsConverter(CloudEventExtensionsConverter... converters) {
		this(converters != null ? Arrays.asList(converters) : Collections.emptyList());
	}

	public CompositeCloudEventExtensionsConverter(List<CloudEventExtensionsConverter> converters) {
		this.converters = converters;
	}

	@Override
	public CloudEventExtensions convert(Message message) {
		Map<String, Object> extensions = new HashMap<>();
		CloudEventExtensions messageExtensions = super.convert(message);
		for (String extensionName : messageExtensions.getExtensionNames()) {
			extensions.put(extensionName, messageExtensions.getExtension(extensionName));
		}

		for (CloudEventExtensionsConverter converter : this.converters) {
			CloudEventExtensions cloudEventExtensions = converter.convert(message);
			for (String extensionName : cloudEventExtensions.getExtensionNames()) {
				if (extensions.containsKey(extensionName)) {
					continue;
				}

				extensions.put(extensionName, cloudEventExtensions.getExtension(extensionName));
			}
		}

		Set<String> extensionNames = extensions.keySet();
		return new CloudEventExtensions() {
			@Nullable
			@Override
			public Object getExtension(String extensionName) {
				return extensions.get(extensionName);
			}

			@Override
			public Set<String> getExtensionNames() {
				return Collections.unmodifiableSet(extensionNames);
			}
		};
	}
}
