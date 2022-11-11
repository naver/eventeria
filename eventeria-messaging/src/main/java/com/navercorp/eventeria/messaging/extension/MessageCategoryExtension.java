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
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.ParametersAreNonnullByDefault;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.cloudevents.CloudEventExtension;
import io.cloudevents.CloudEventExtensions;
import io.cloudevents.core.extensions.impl.ExtensionUtils;
import io.cloudevents.core.provider.ExtensionProvider;

import com.navercorp.eventeria.messaging.contract.cloudevents.extension.CloudEventExtensionsUtil;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensions;

@ParametersAreNonnullByDefault
public final class MessageCategoryExtension implements CloudEventExtension {
	public static final String MESSAGE_CATEGORY_EXTENSION = "messagecategory";
	public static final Set<String> MESSAGE_CATEGORY_EXTENSION_KEYS = Collections.singleton(MESSAGE_CATEGORY_EXTENSION);
	public static final String MESSAGE_CATEGORY_DELIMITER = ",";
	private static final Logger LOG = LoggerFactory.getLogger(MessageCategoryExtension.class);

	static {
		ExtensionProvider.getInstance()
			.registerExtension(MessageCategoryExtension.class, MessageCategoryExtension::new);
	}

	private Set<MessageCategory> messageCategories;

	public static MessageCategoryExtension parseExtension(CloudEventExtensions cloudEventExtensions) {
		return ExtensionProvider.getInstance().parseExtension(MessageCategoryExtension.class, cloudEventExtensions);
	}

	public static MessageCategoryExtension parseExtension(MessageExtensions messageExtensions) {
		return parseExtension(CloudEventExtensionsUtil.toCloudEventExtensions(messageExtensions));
	}

	@Override
	public void readFrom(CloudEventExtensions extensions) {
		Object messageCategory = extensions.getExtension(MESSAGE_CATEGORY_EXTENSION);
		if (messageCategory == null) {
			this.messageCategories = null;
			return;
		}

		Set<MessageCategory> categories = new HashSet<>();
		String[] messageCategories = messageCategory.toString().split(MESSAGE_CATEGORY_DELIMITER);
		for (String category : messageCategories) {
			try {
				categories.add(MessageCategory.valueOf(category));
			} catch (Exception ex) {
				LOG.warn("Can not find MessageCategory enum value. category: {}", category, ex);
			}
		}

		this.messageCategories = categories;
	}

	@Override
	public Set<MessageCategory> getValue(String key) throws IllegalArgumentException {
		if (MESSAGE_CATEGORY_EXTENSION.equals(key)) {
			return this.messageCategories != null
				? Collections.unmodifiableSet(this.messageCategories)
				: Collections.emptySet();
		}

		throw ExtensionUtils.generateInvalidKeyException(this.getClass(), key);
	}

	@Override
	public Set<String> getKeys() {
		return MESSAGE_CATEGORY_EXTENSION_KEYS;
	}

	public Set<MessageCategory> getMessageCategories() {
		return this.messageCategories != null
			? Collections.unmodifiableSet(this.messageCategories)
			: Collections.emptySet();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		MessageCategoryExtension that = (MessageCategoryExtension)obj;
		return Objects.equals(this.messageCategories, that.messageCategories);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.messageCategories);
	}

	@Override
	public String toString() {
		return "MessageCategoryExtension{"
			+ "messageCategories='" + messageCategories + '\''
			+ '}';
	}

	public enum MessageCategory {
		MESSAGE, COMMAND, EVENT, DOMAIN_EVENT, INTEGRATION_EVENT, TIMER
	}
}
