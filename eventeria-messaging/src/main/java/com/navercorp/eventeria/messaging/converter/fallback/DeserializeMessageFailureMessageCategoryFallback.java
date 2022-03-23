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

package com.navercorp.eventeria.messaging.converter.fallback;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.SimpleMessage;
import com.navercorp.eventeria.messaging.contract.command.SimpleCommand;
import com.navercorp.eventeria.messaging.contract.event.SimpleEvent;
import com.navercorp.eventeria.messaging.contract.serializer.MessageDeserializer;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension;
import com.navercorp.eventeria.messaging.extension.MessageCategoryExtension.MessageCategory;

public final class DeserializeMessageFailureMessageCategoryFallback implements DeserializeMessageFailureFallback {
	private static final Logger logger =
		LoggerFactory.getLogger(DeserializeMessageFailureMessageCategoryFallback.class);

	private final MessageDeserializer messageDeserializer;

	public DeserializeMessageFailureMessageCategoryFallback(MessageDeserializer messageDeserializer) {
		this.messageDeserializer = messageDeserializer;
	}

	@Override
	public Message fallback(CloudEvent cloudEvent, Throwable throwable) {
		logger.warn(
			"Deserialize message is failed. [FALLBACK] deserialize simple message by message category. cloudEvent: {}",
			cloudEvent,
			throwable
		);

		byte[] serializedDate = cloudEvent.getData().toBytes();
		MessageCategoryExtension messageCategoryExtension = MessageCategoryExtension.parseExtension(cloudEvent);
		Set<MessageCategory> messageCategories = messageCategoryExtension.getMessageCategories();

		if (messageCategories.contains(MessageCategory.DOMAIN_EVENT)
			|| messageCategories.contains(MessageCategory.EVENT)
			|| messageCategories.contains(MessageCategory.INTEGRATION_EVENT)
		) {
			return this.messageDeserializer.deserialize(serializedDate, SimpleEvent.class);
		}

		if (messageCategories.contains(MessageCategory.COMMAND)) {
			return this.messageDeserializer.deserialize(serializedDate, SimpleCommand.class);
		}

		return this.messageDeserializer.deserialize(serializedDate, SimpleMessage.class);
	}
}
