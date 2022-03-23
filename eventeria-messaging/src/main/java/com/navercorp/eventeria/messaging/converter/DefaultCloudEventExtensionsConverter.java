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

import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import io.cloudevents.CloudEventExtensions;
import io.cloudevents.lang.Nullable;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.extension.EmptyCloudEventExtensions;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensions;

@ParametersAreNonnullByDefault
public class DefaultCloudEventExtensionsConverter implements CloudEventExtensionsConverter {
	@Override
	public CloudEventExtensions convert(Message message) {
		if (message instanceof CloudEventExtensions) {
			return (CloudEventExtensions)message;
		}

		if (!(message instanceof MessageExtensions)) {
			return EmptyCloudEventExtensions.INSTANCE;
		}

		MessageExtensions messageExtensions = (MessageExtensions)message;
		return new CloudEventExtensions() {
			@Nullable
			@Override
			public Object getExtension(String extensionName) {
				return messageExtensions.getExtension(extensionName);
			}

			@Override
			public Set<String> getExtensionNames() {
				return messageExtensions.getExtensionNames();
			}
		};
	}
}
