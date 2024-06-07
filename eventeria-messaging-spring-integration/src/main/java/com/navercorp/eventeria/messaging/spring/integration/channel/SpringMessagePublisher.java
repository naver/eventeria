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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.support.GenericMessage;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.channel.MessagePublisher;
import com.navercorp.eventeria.messaging.contract.event.ApplicationEvent;
import com.navercorp.eventeria.messaging.spring.integration.channel.fallback.FallbackPolicy;

/**
 * A implementation of {@link MessagePublisher} and {@link org.springframework.messaging.MessageChannel}
 * to publish {@link Message}s.
 */
public class SpringMessagePublisher extends DirectChannel implements MessagePublisher, ApplicationEventPublisherAware {
	private final FallbackPolicy fallbackPolicy;
	@Nullable
	private final Executor executor;
	private ApplicationEventPublisher applicationEventPublisher = e -> {
	};

	public SpringMessagePublisher() {
		this(FallbackPolicy.THROW_EXCEPTION, null);
	}

	public SpringMessagePublisher(FallbackPolicy fallbackPolicy, Executor executor) {
		this.fallbackPolicy = fallbackPolicy;
		this.executor = executor;
	}

	/**
	 * Converts {@link Message}s to a {@link org.springframework.messaging.Message}
	 * and publish to outbound channel declared by spring-integration.<br/>
	 * Each message are published by {@link ApplicationEventPublisher} as well.
	 *
	 * @param messages
	 */
	@Override
	public void publish(Iterable<? extends Message> messages) {
		if (this.getSubscriberCount() > 0) {
			List<Message> externals = new ArrayList<>();
			for (Message message : messages) {
				if (this.isExternalMessage(message)) {
					externals.add(message);
				}
			}

			if (!externals.isEmpty()) {
				this.send(new GenericMessage<>(externals));
			}
		}

		messages.forEach(this.applicationEventPublisher::publishEvent);
	}

	/**
	 * Converts a {@link Message} to a {@link org.springframework.messaging.Message}
	 * and publish to outbound channel declared by spring-integration.<br/>
	 * The message is published by {@link ApplicationEventPublisher} as well.
	 *
	 * @param message
	 */
	@Override
	public void publish(Message message) {
		if (this.getSubscriberCount() > 0 && this.isExternalMessage(message)) {
			this.send(new GenericMessage<>(message));
		}

		this.applicationEventPublisher.publishEvent(message);
	}

	@Override
	public boolean send(org.springframework.messaging.Message<?> message) {
		return this.sendExternal(message, -1);
	}

	@Override
	public boolean send(org.springframework.messaging.Message<?> message, long timeout) {
		return this.sendExternal(message, timeout);
	}

	private boolean sendExternal(org.springframework.messaging.Message<?> message, long timeout) {
		if (this.executor == null) {
			return this.doSendExternal(message, timeout);
		}

		try {
			this.executor.execute(() -> this.doSendExternal(message, timeout));
			return true;
		} catch (Exception ex) {
			logger.error(ex, "External message send is failed with executor. message: " + message);
			return false;
		}
	}

	private boolean doSendExternal(org.springframework.messaging.Message<?> message, long timeout) {
		try {
			Boolean result = super.send(message, timeout);

			if (Boolean.TRUE.equals(result)) {
				return true;
			}

			throw new MessageDeliveryException(message, "External message send is failed.");
		} catch (Exception ex) {
			if (this.fallbackPolicy == FallbackPolicy.IGNORE) {
				logger.warn(ex,
					"Send external message is failed. But fallback is ignored. message: " + message.toString());
				return false;
			}

			throw ex;
		}
	}

	@Override
	public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
		this.applicationEventPublisher = applicationEventPublisher;
	}

	private boolean isExternalMessage(Message message) {
		if (!(message instanceof ApplicationEvent applicationEvent)) {
			return true;
		}

		return applicationEvent.isExternalEvent();
	}
}
