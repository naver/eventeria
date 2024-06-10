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

package com.navercorp.eventeria.messaging.spring.integration.channel;

import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.messaging.support.GenericMessage;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.channel.MessageHandler;

/**
 * A implementation of {@link com.navercorp.eventeria.messaging.contract.channel.MessageHandler}
 * and {@link org.springframework.messaging.MessageChannel} to subscribe a {@link Message}.
 */
public class SpringMessageHandler extends PublishSubscribeChannel
	implements MessageHandler, ApplicationEventPublisherAware {
	private boolean publishApplicationEventPublisher;
	private ApplicationEventPublisher applicationEventPublisher = e -> {
	};

	@Override
	protected boolean doSend(org.springframework.messaging.Message<?> message, long timeout) {
		boolean result = super.doSend(message, timeout);
		if (this.publishApplicationEventPublisher) {
			this.applicationEventPublisher.publishEvent(message);
		}
		return result;
	}

	/**
	 * Converts a {@link Message} to a {@link org.springframework.messaging.Message}
	 * and publish to inbound channel (your application logic) which is declared by spring-integration.
	 *
	 * @param message
	 */
	@Override
	public void handle(Message message, Map<String, Object> headers) {
		this.send(new GenericMessage<>(message, headers));
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	public void setPublishApplicationEventPublisher(boolean publishApplicationEventPublisher) {
		this.publishApplicationEventPublisher = publishApplicationEventPublisher;
	}
}
