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

package com.navercorp.eventeria.messaging.spring.integration.router;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.router.PayloadTypeRouter;
import org.springframework.messaging.MessageChannel;

import com.navercorp.eventeria.messaging.contract.Message;

public class MessagePayloadTypeRouter extends PayloadTypeRouter {
	private final String messageHandlerBeanName;
	private final Map<String, MessageChannel> mappingChannels = new HashMap<>();

	@SuppressWarnings({"rawtypes", "unchecked"})
	private MessagePayloadTypeRouter(
		String messageHandlerBeanName,
		Map<Class, Consumer> mappings
	) {
		this.messageHandlerBeanName = messageHandlerBeanName;
		mappings.forEach((routePayloadType, consumer) -> {
			if (routePayloadType == List.class) {
				this.setBatchChannelMapping(consumer);
			} else {
				this.setChannelMapping(routePayloadType, consumer);
			}
		});
		super.setResolutionRequired(false);
		super.setDefaultOutputChannel(new NullChannel());
	}

	public static RouterRegister register(String messageHandlerBeanName) {
		Objects.requireNonNull(messageHandlerBeanName,
			"The parameter 'messageHandlerBeanName' can not be null.");
		return new RouterRegister(messageHandlerBeanName);
	}

	@SuppressWarnings("unchecked")
	private <T> void setChannelMapping(
		Class<T> routePayloadType,
		Consumer<T> consumer
	) {
		String routeChannelName = this.messageHandlerBeanName + "-" + routePayloadType.getName();
		FixedSubscriberChannel routeChannel = new FixedSubscriberChannel(message ->
			consumer.accept((T)message.getPayload())
		);
		super.setChannelMapping(routePayloadType.getName(), routeChannelName);
		this.mappingChannels.put(routeChannelName, routeChannel);
	}

	@SuppressWarnings({"rawtypes"})
	private void setBatchChannelMapping(Consumer<List> consumer) {
		String routeChannelName = this.messageHandlerBeanName + "-batch";
		FixedSubscriberChannel routeChannel = new FixedSubscriberChannel(message ->
			consumer.accept((List)message.getPayload())
		);
		super.setChannelMapping(List.class.getName(), routeChannelName);
		this.mappingChannels.put(routeChannelName, routeChannel);
	}

	@Override
	protected void onInit() {
		super.onInit();

		ApplicationContext appContext = this.getApplicationContext();
		if (!(appContext instanceof GenericApplicationContext)) {
			throw new RuntimeException(
				"ApplicationContext should be instance of GenericApplicationContext to register bean. type: "
					+ appContext.getClass().getName()
			);
		}

		GenericApplicationContext applicationContext = (GenericApplicationContext)appContext;
		this.mappingChannels.forEach((beanName, channel) ->
			applicationContext.registerBean(beanName, MessageChannel.class, () -> channel)
		);
	}

	@SuppressWarnings("rawtypes")
	public static class RouterRegister {
		private final String messageHandlerBeanName;
		private final Map<Class, Consumer> mappings = new HashMap<>();

		private RouterRegister(String messageHandlerBeanName) {
			this.messageHandlerBeanName = messageHandlerBeanName;
		}

		public <T extends Message> RouterRegister ignore(Class<T> routePayloadType) {
			Consumer doNothing = message -> {
			};
			this.mappings.put(routePayloadType, doNothing);
			return this;
		}

		public <T extends Message> RouterRegister route(Class<T> routePayloadType, Consumer<T> consumer) {
			this.mappings.put(routePayloadType, consumer);
			return this;
		}

		@SuppressWarnings("rawtypes")
		public <T extends Message> RouterRegister batch(Consumer<List<T>> consumer) {
			this.mappings.put(List.class, consumer);
			return this;
		}

		public MessagePayloadTypeRouter done() {
			return new MessagePayloadTypeRouter(this.messageHandlerBeanName, this.mappings);
		}
	}
}
