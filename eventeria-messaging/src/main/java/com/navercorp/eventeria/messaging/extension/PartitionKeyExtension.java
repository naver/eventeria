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

package com.navercorp.eventeria.messaging.extension;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.cloudevents.CloudEventExtension;
import io.cloudevents.CloudEventExtensions;
import io.cloudevents.core.extensions.impl.ExtensionUtils;
import io.cloudevents.core.provider.ExtensionProvider;

import com.navercorp.eventeria.messaging.contract.cloudevents.extension.CloudEventExtensionsUtil;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensions;

@ParametersAreNonnullByDefault
public final class PartitionKeyExtension implements CloudEventExtension {
	public static final String PARTITION_KEY_EXTENSION = "partitionkey";
	public static final Set<String> PARTITION_KEY_EXTENSION_KEYS = Collections.singleton(PARTITION_KEY_EXTENSION);

	private String partitionKey;

	static {
		ExtensionProvider.getInstance()
			.registerExtension(PartitionKeyExtension.class, PartitionKeyExtension::new);
	}

	public static PartitionKeyExtension parseExtension(CloudEventExtensions cloudEventExtensions) {
		return ExtensionProvider.getInstance().parseExtension(PartitionKeyExtension.class, cloudEventExtensions);
	}

	public static PartitionKeyExtension parseExtension(MessageExtensions messageExtensions) {
		return parseExtension(CloudEventExtensionsUtil.toCloudEventExtensions(messageExtensions));
	}

	@Override
	public void readFrom(CloudEventExtensions extensions) {
		Object partitionKey = extensions.getExtension(PARTITION_KEY_EXTENSION);
		if (partitionKey != null) {
			this.partitionKey = partitionKey.toString();
		}
	}

	@Nullable
	@Override
	public String getValue(String key) {
		if (PARTITION_KEY_EXTENSION.equals(key)) {
			return this.partitionKey;
		}

		throw ExtensionUtils.generateInvalidKeyException(this.getClass(), key);
	}

	@Override
	public Set<String> getKeys() {
		return PARTITION_KEY_EXTENSION_KEYS;
	}

	@Nullable
	public String getPartitionKey() {
		return this.partitionKey;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PartitionKeyExtension that = (PartitionKeyExtension)obj;
		return Objects.equals(this.partitionKey, that.partitionKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.partitionKey);
	}

	@Override
	public String toString() {
		return "PartitionKeyExtension{"
			+ "partitionKey='" + partitionKey + '\''
			+ '}';
	}
}
