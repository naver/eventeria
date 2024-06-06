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

package com.navercorp.eventeria.messaging.spring.integration.dsl;

import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.messaging.SubscribableChannel;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.filter.CloudEventFilter;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessageHandler;

/**
 * An {@link IntegrationFlow} template to support conversion of {@link CloudEvent} to {@link Message},
 * and to subscribe {@link CloudEvent} from input channel.
 */
public class MessageSubscriberIntegrationAdapter extends IntegrationFlowAdapter {
	private final SubscribableChannel inputChannel;
	private final CloudEventToMessageConverter messageConverter;
	private final SpringMessageHandler messageHandler;
	private final CloudEventFilter cloudEventFilter;

	public MessageSubscriberIntegrationAdapter(
		SubscribableChannel inputChannel,
		CloudEventToMessageConverter messageConverter,
		SpringMessageHandler messageHandler
	) {
		this(
			inputChannel,
			messageConverter,
			messageHandler,
			(cloudEvent) -> true
		);
	}

	/**
	 * @param inputChannel message channel that supports subscribing {@link CloudEvent}.
	 * @param messageConverter {@link CloudEvent} to {@link Message} converter.
	 * @param messageHandler subscriber of {@link Message} to send {@link Message} to inbound application channel
	 * @param cloudEventFilter
	 */
	public MessageSubscriberIntegrationAdapter(
		SubscribableChannel inputChannel,
		CloudEventToMessageConverter messageConverter,
		SpringMessageHandler messageHandler,
		CloudEventFilter cloudEventFilter
	) {
		this.inputChannel = inputChannel;
		this.messageConverter = messageConverter;
		this.messageHandler = messageHandler;
		this.cloudEventFilter = cloudEventFilter;
	}

	@Override
	protected IntegrationFlowDefinition<?> buildFlow() {
		return IntegrationFlow.from(this.getInputSubscribableChannel())
			.filter(CloudEvent.class, this.getCloudEventFilter()::accept)
			.transform(CloudEvent.class, this.getMessageConverter()::convert)
			.channel(this.getSpringMessageHandler());
	}

	/**
	 * @return message channel that supports subscribing {@link CloudEvent}.
	 */
	protected SubscribableChannel getInputSubscribableChannel() {
		return this.inputChannel;
	}

	/**
	 * @return {@link CloudEvent} to {@link Message} converter.
	 */
	protected CloudEventToMessageConverter getMessageConverter() {
		return this.messageConverter;
	}

	/**
	 * @return subscriber of {@link Message} to send {@link Message} to inbound application channel
	 */
	protected SpringMessageHandler getSpringMessageHandler() {
		return this.messageHandler;
	}

	protected CloudEventFilter getCloudEventFilter() {
		return this.cloudEventFilter;
	}
}
