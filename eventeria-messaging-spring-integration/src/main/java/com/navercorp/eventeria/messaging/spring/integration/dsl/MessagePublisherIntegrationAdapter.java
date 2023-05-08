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

import java.util.function.Consumer;

import org.springframework.integration.dsl.HeaderEnricherSpec;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.integration.dsl.IntegrationFlowDefinition;
import org.springframework.messaging.MessageChannel;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.header.CloudEventHeaderMapper;
import com.navercorp.eventeria.messaging.filter.CloudEventFilter;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessagePublisher;

public class MessagePublisherIntegrationAdapter extends IntegrationFlowAdapter {
	private final SpringMessagePublisher messagePublisher;
	private final MessageToCloudEventConverter messageConverter;
	private final CloudEventHeaderMapper cloudEventHeaderMapper;
	private final MessageChannel outputChannel;
	private final CloudEventFilter cloudEventFilter;

	public MessagePublisherIntegrationAdapter(
		SpringMessagePublisher messagePublisher,
		MessageToCloudEventConverter messageConverter,
		CloudEventHeaderMapper cloudEventHeaderMapper,
		MessageChannel outputChannel
	) {
		this(
			messagePublisher,
			messageConverter,
			cloudEventHeaderMapper,
			outputChannel,
			(cloudEvent) -> true
		);
	}

	public MessagePublisherIntegrationAdapter(
		SpringMessagePublisher messagePublisher,
		MessageToCloudEventConverter messageConverter,
		CloudEventHeaderMapper cloudEventHeaderMapper,
		MessageChannel outputChannel,
		CloudEventFilter cloudEventFilter
	) {
		this.messagePublisher = messagePublisher;
		this.messageConverter = messageConverter;
		this.cloudEventHeaderMapper = cloudEventHeaderMapper;
		this.outputChannel = outputChannel;
		this.cloudEventFilter = cloudEventFilter;
	}

	@Override
	protected IntegrationFlowDefinition<?> buildFlow() {
		return IntegrationFlow.from(this.getMessagePublisher())
			.split()
			.transform(Message.class, this.getMessageConverter()::convert)
			.enrichHeaders(this.enrichCloudEventHeaderSpec())
			.filter(this.getCloudEventFilter()::accept)
			.channel(this.getOutputMessageChannel());
	}

	protected SpringMessagePublisher getMessagePublisher() {
		return this.messagePublisher;
	}

	protected MessageToCloudEventConverter getMessageConverter() {
		return this.messageConverter;
	}

	protected CloudEventHeaderMapper getCloudEventHeaderMapper() {
		return this.cloudEventHeaderMapper;
	}

	protected CloudEventFilter getCloudEventFilter() {
		return this.cloudEventFilter;
	}

	protected MessageChannel getOutputMessageChannel() {
		return this.outputChannel;
	}

	protected Consumer<HeaderEnricherSpec> enrichCloudEventHeaderSpec() {
		return he -> he
			.shouldSkipNulls(true)
			.defaultOverwrite(true)
			.messageProcessor(m -> this.getCloudEventHeaderMapper().toHeaderMap((CloudEvent)m.getPayload()));
	}
}
