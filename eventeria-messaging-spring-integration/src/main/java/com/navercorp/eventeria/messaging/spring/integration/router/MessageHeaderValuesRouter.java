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

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Consumer;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.integration.channel.FixedSubscriberChannel;
import org.springframework.integration.channel.NullChannel;
import org.springframework.integration.router.AbstractMappingMessageRouter;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.CollectionUtils;

import com.navercorp.eventeria.messaging.contract.Message;

public class MessageHeaderValuesRouter extends AbstractMappingMessageRouter {

	private final String messageHandlerBeanName;
	private final Map<Entry<String, Object>, MessageChannel> mappingChannels = new LinkedHashMap<>();

	@SuppressWarnings({"rawtypes", "unchecked"})
	private MessageHeaderValuesRouter(
		String messageHandlerBeanName,
		Map<Entry<String, Object>, Consumer> mappings,
		Consumer defaultRoute
	) {
		this.messageHandlerBeanName = messageHandlerBeanName;
		mappings.forEach(this::setChannelMapping);
		super.setResolutionRequired(false);

		if (defaultRoute == null) {
			super.setDefaultOutputChannel(new NullChannel());
		} else {
			FixedSubscriberChannel defaultRouteChannel = new FixedSubscriberChannel(message ->
				defaultRoute.accept(message.getPayload())
			);
			super.setDefaultOutputChannel(defaultRouteChannel);
		}
	}

	public static RouterRegister register(String messageHandlerBeanName) {
		Objects.requireNonNull(messageHandlerBeanName,
			"The parameter 'messageHandlerBeanName' can not be null.");
		return new MessageHeaderValuesRouter.RouterRegister(messageHandlerBeanName);
	}

	@SuppressWarnings("unchecked")
	private <T> void setChannelMapping(Entry<String, Object> headerKeyValue, Consumer<T> consumer) {
		FixedSubscriberChannel routeChannel = new FixedSubscriberChannel(message ->
			consumer.accept((T)message.getPayload())
		);
		String routerName = getRouterName(headerKeyValue);
		super.setChannelMapping(routerName, routerName);
		this.mappingChannels.put(headerKeyValue, routeChannel);
	}

	private String getRouterName(Entry<String, Object> headerKeyValue) {
		String headerKey = headerKeyValue.getKey();
		String headerValue = Objects.toString(headerKeyValue.getValue(), "null");
		String channelKey = headerKey + "-" + headerValue;
		return this.messageHandlerBeanName + "-" + channelKey;
	}

	@Override
	protected List<Object> getChannelKeys(org.springframework.messaging.Message<?> message) {
		if (CollectionUtils.isEmpty(getChannelMappings())) {
			return Collections.emptyList();
		}

		for (Entry<Entry<String, Object>, MessageChannel> candidate : mappingChannels.entrySet()) {
			Entry<String, Object> targetHeader = candidate.getKey();
			Object value = message.getHeaders().get(targetHeader.getKey());

			if (targetHeader.getValue() == null) {
				if (value == null) {
					return Collections.singletonList(getRouterName(targetHeader));
				}
				return Collections.emptyList();
			}
			if (targetHeader.getValue().equals(value)) {
				return Collections.singletonList(getRouterName(targetHeader));
			}
		}
		return Collections.emptyList();
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
		this.mappingChannels.forEach((headerKeyValue, channel) ->
			applicationContext.registerBean(getRouterName(headerKeyValue), MessageChannel.class, () -> channel)
		);
	}

	@SuppressWarnings("rawtypes")
	public static class RouterRegister {
		private final String messageHandlerBeanName;
		private final Map<Entry<String, Object>, Consumer> mappings = new LinkedHashMap<>();
		private Consumer defaultRoute;

		private RouterRegister(String messageHandlerBeanName) {
			this.messageHandlerBeanName = messageHandlerBeanName;
		}

		public <T extends Message> RouterRegister defaultRoute(
			Consumer<T> consumer) {
			this.defaultRoute = consumer;
			return this;
		}

		public RouterRegister ignore(String key, Object value) {
			Consumer doNothing = message -> {
			};

			this.mappings.put(new SimpleEntry<>(key, value), doNothing);
			return this;
		}

		public <T extends Message> RouterRegister route(String key, Object value, Consumer<T> consumer) {
			Objects.requireNonNull(key, "The parameter 'key' can not be null.");
			this.mappings.put(new SimpleEntry<>(key, value), consumer);
			return this;
		}

		public MessageHeaderValuesRouter done() {
			return new MessageHeaderValuesRouter(this.messageHandlerBeanName, this.mappings, defaultRoute);
		}
	}
}
