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
public final class CloudEventTypeAliasExtension implements CloudEventExtension {
	public static final String TYPE_ALIAS_EXTENSION = "typealias";
	public static final Set<String> TYPE_ALIAS_EXTENSION_KEYS = Collections.singleton(TYPE_ALIAS_EXTENSION);

	private String typeAlias;

	static {
		ExtensionProvider.getInstance()
			.registerExtension(CloudEventTypeAliasExtension.class, CloudEventTypeAliasExtension::new);
	}

	public static CloudEventTypeAliasExtension parseExtension(CloudEventExtensions cloudEventExtensions) {
		return ExtensionProvider.getInstance().parseExtension(CloudEventTypeAliasExtension.class, cloudEventExtensions);
	}

	public static CloudEventTypeAliasExtension parseExtension(MessageExtensions messageExtensions) {
		return parseExtension(CloudEventExtensionsUtil.toCloudEventExtensions(messageExtensions));
	}

	@Override
	public void readFrom(CloudEventExtensions extensions) {
		Object typeAlias = extensions.getExtension(TYPE_ALIAS_EXTENSION);
		if (typeAlias != null) {
			this.typeAlias = typeAlias.toString();
		}
	}

	@Nullable
	@Override
	public String getValue(String key) {
		if (TYPE_ALIAS_EXTENSION.equals(key)) {
			return this.typeAlias;
		}

		throw ExtensionUtils.generateInvalidKeyException(this.getClass(), key);
	}

	@Override
	public Set<String> getKeys() {
		return TYPE_ALIAS_EXTENSION_KEYS;
	}

	@Nullable
	public String getTypeAlias() {
		return this.typeAlias;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		CloudEventTypeAliasExtension that = (CloudEventTypeAliasExtension)obj;
		return Objects.equals(this.typeAlias, that.typeAlias);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.typeAlias);
	}

	@Override
	public String toString() {
		return "CloudEventTypeAliasExtension{"
			+ "typeAlias='" + typeAlias + '\''
			+ '}';
	}
}
