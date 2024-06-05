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

import static java.util.stream.Collectors.joining;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.cloudevents.CloudEventExtensions;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.DomainEvent;
import com.navercorp.eventeria.messaging.contract.event.Event;
import com.navercorp.eventeria.messaging.contract.event.IntegrationEvent;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension.MessageCategory;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;
import com.navercorp.eventeria.timer.contract.TimerMessage;

/**
 * Converts a {@link Message} to messagecategory {@link CloudEventExtensions}
 *
 * @see MessageCategoryExtension
 */
@ParametersAreNonnullByDefault
public final class MessageCategoryExtensionsConverter implements CloudEventExtensionsConverter {
	@Override
	public CloudEventExtensions convert(Message message) {
		List<MessageCategory> categories = new ArrayList<>();
		categories.add(MessageCategory.MESSAGE);

		if (message instanceof Event) {
			categories.add(MessageCategory.EVENT);
		}
		if (message instanceof DomainEvent) {
			categories.add(MessageCategory.DOMAIN_EVENT);
		}
		if (message instanceof IntegrationEvent) {
			categories.add(MessageCategory.INTEGRATION_EVENT);
		}
		if (message instanceof Command) {
			categories.add(MessageCategory.COMMAND);
		}
		if (message instanceof TimerMessage) {
			categories.add(MessageCategory.TIMER);
		}

		String messageCategories = categories.stream()
			.map(MessageCategory::name)
			.collect(joining(MessageCategoryExtension.MESSAGE_CATEGORY_DELIMITER));

		return new CloudEventExtensions() {
			@Nullable
			@Override
			public Object getExtension(String extensionName) {
				if (MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION.equals(extensionName)) {
					return messageCategories;
				}

				return null;
			}

			@Override
			public Set<String> getExtensionNames() {
				return MessageCategoryExtension.MESSAGE_CATEGORY_EXTENSION_KEYS;
			}
		};
	}
}
