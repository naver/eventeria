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

import java.util.UUID;

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
 * An {@link IntegrationFlow} template supports batch messages.
 *
 * @see MessageSubscriberIntegrationAdapter
 */
public class MessageBatchSubscriberIntegrationAdapter extends IntegrationFlowAdapter {
	private final SubscribableChannel inputChannel;
	private final CloudEventToMessageConverter messageConverter;
	private final SpringMessageHandler messageHandler;
	private final CloudEventFilter cloudEventFilter;

	public MessageBatchSubscriberIntegrationAdapter(
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

	public MessageBatchSubscriberIntegrationAdapter(
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
			.enrichHeaders(eh -> eh.correlationId(UUID.randomUUID()))
			.splitWith(sp -> sp.applySequence(true).getObject())
			.filter(CloudEvent.class, this.getCloudEventFilter()::accept)
			.transform(CloudEvent.class, this.getMessageConverter()::convert)
			.aggregate(sp -> sp.expireGroupsUponCompletion(true))
			.channel(this.getSpringMessageHandler());
	}

	protected SubscribableChannel getInputSubscribableChannel() {
		return this.inputChannel;
	}

	protected CloudEventToMessageConverter getMessageConverter() {
		return this.messageConverter;
	}

	protected SpringMessageHandler getSpringMessageHandler() {
		return this.messageHandler;
	}

	protected CloudEventFilter getCloudEventFilter() {
		return this.cloudEventFilter;
	}
}
