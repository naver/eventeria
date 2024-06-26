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
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.messaging.MessageChannel;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.header.CloudEventHeaderMapper;
import com.navercorp.eventeria.messaging.filter.CloudEventFilter;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessagePublisher;
import com.navercorp.eventeria.timer.spring.integration.handler.SpringTimerMessageHandler;

/**
 * Extended implementation of {@link MessagePublisherIntegrationAdapter} which supports
 * publishing {@link com.navercorp.eventeria.timer.contract.TimerMessage}s
 */
public class TimerMessagePublisherIntegrationAdapter extends MessagePublisherIntegrationAdapter {
	private final SpringTimerMessageHandler springTimerMessageHandler;

	public TimerMessagePublisherIntegrationAdapter(
		SpringMessagePublisher messagePublisher,
		MessageToCloudEventConverter messageConverter,
		CloudEventHeaderMapper cloudEventHeaderMapper,
		MessageChannel outputChannel,
		SpringTimerMessageHandler springTimerMessageHandler
	) {
		this(
			messagePublisher,
			messageConverter,
			cloudEventHeaderMapper,
			outputChannel,
			(cloudEvent) -> true,
			springTimerMessageHandler
		);
	}

	/**
	 * @param messagePublisher message channel that supports publishing {@link Message}.
	 * @param messageConverter {@link Message} to {@link CloudEvent} converter.
	 * @param cloudEventHeaderMapper mapper to send {@link io.cloudevents.CloudEventExtensions} values to
	 *                               headers of {@link org.springframework.messaging.Message}.
	 * @param outputChannel output channel of spring-integration to send other system.
	 * @param cloudEventFilter
	 * @param springTimerMessageHandler publish/subscribe channel
	 *                                  for {@link com.navercorp.eventeria.timer.contract.TimerMessage}/
	 */
	public TimerMessagePublisherIntegrationAdapter(
		SpringMessagePublisher messagePublisher,
		MessageToCloudEventConverter messageConverter,
		CloudEventHeaderMapper cloudEventHeaderMapper,
		MessageChannel outputChannel,
		CloudEventFilter cloudEventFilter,
		SpringTimerMessageHandler springTimerMessageHandler
	) {
		super(
			messagePublisher,
			messageConverter,
			cloudEventHeaderMapper,
			outputChannel,
			cloudEventFilter
		);
		this.springTimerMessageHandler = springTimerMessageHandler;
	}

	@Override
	protected IntegrationFlowDefinition<?> buildFlow() {
		return IntegrationFlow.from(this.getMessagePublisher())
			.split()
			.handle(this.getSpringTimerMessageHandler())
			.transform(Message.class, this.getMessageConverter()::convert)
			.enrichHeaders(this.enrichCloudEventHeaderSpec())
			.filter(this.getCloudEventFilter()::accept)
			.channel(this.getOutputMessageChannel());
	}

	/**
	 * @return publish/subscribe channel for {@link com.navercorp.eventeria.timer.contract.TimerMessage}
	 */
	protected SpringTimerMessageHandler getSpringTimerMessageHandler() {
		return this.springTimerMessageHandler;
	}
}
