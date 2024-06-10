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

package com.navercorp.eventeria.fake.spring.cloud.stream.binder.kafka;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

import javax.annotation.Nullable;

import org.springframework.cloud.stream.binder.BinderFactory;
import org.springframework.cloud.stream.binder.ExtendedPropertiesBinder;
import org.springframework.cloud.stream.binder.kafka.properties.KafkaConsumerProperties;
import org.springframework.cloud.stream.config.BindingProperties;
import org.springframework.cloud.stream.config.BindingServiceProperties;
import org.springframework.cloud.stream.messaging.DirectWithAttributesChannel;
import org.springframework.kafka.transaction.KafkaAwareTransactionManager;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.ChannelBinder;

/**
 * Do bind and return bindings in type of FakeBindingSubscribableChannel.
 */
public class FakeKafkaChannelBinder implements ChannelBinder {
	private final BindingServiceProperties bindingServiceProperties;
	private final BinderFactory binderFactory;
	private final FakeKafkaMessageAccumulator kafkaMessageAccumulator;
	private final TransactionTemplate kafkaTransactionTemplate;
	@Nullable
	private final Executor executor;
	private final Map<String, FakeBindingSubscribableChannel> outboundChannels;
	private final Map<String, FakeBindingSubscribableChannel> inboundChannels;

	public FakeKafkaChannelBinder(
		BindingServiceProperties bindingServiceProperties,
		BinderFactory binderFactory,
		FakeKafkaMessageAccumulator kafkaMessageAccumulator,
		@Nullable KafkaAwareTransactionManager<?, ?> kafkaAwareTransactionManager,
		@Nullable Executor executor
	) {
		this.bindingServiceProperties = bindingServiceProperties;
		this.binderFactory = binderFactory;
		this.kafkaMessageAccumulator = kafkaMessageAccumulator;
		this.kafkaTransactionTemplate = kafkaAwareTransactionManager != null
			? new TransactionTemplate(kafkaAwareTransactionManager)
			: null;
		this.executor = executor;
		this.outboundChannels = new HashMap<>();
		this.inboundChannels = new HashMap<>();
		this.initializeBinding();
	}

	private void initializeBinding() {
		MultiValueMap<String, FakeBindingSubscribableChannel> topicOutboundChannels = new LinkedMultiValueMap<>();
		MultiValueMap<String, FakeBindingSubscribableChannel> topicInboundChannels = new LinkedMultiValueMap<>();

		// oubound, inbound, dlq channel
		Map<String, FakeBindingSubscribableChannel> inboundDlqChannels = new HashMap<>();
		MultiValueMap<String, FakeBindingSubscribableChannel> topicDlqChannels = new LinkedMultiValueMap<>();

		Set<String> channels = this.bindingServiceProperties.getBindings().keySet();
		channels.forEach(it -> {
			BindingProperties bindingProperties = this.bindingServiceProperties.getBindingProperties(it);
			String topic = bindingProperties.getDestination();

			// consumer
			if (bindingProperties.getGroup() != null) {
				FakeBindingSubscribableChannel inboundChannel = new FakeBindingSubscribableChannel(
					topic,
					"input",
					bindingProperties.getConsumer().isBatchMode(),
					this.kafkaTransactionTemplate != null && !bindingProperties.getConsumer().isBatchMode()
				);

				inboundChannel.setBeanName(it);
				topicInboundChannels.add(topic, inboundChannel);

				this.inboundChannels.put(it, inboundChannel);

				// dlq 생성
				String binderConfigurationName = this.bindingServiceProperties.getBinder(it);
				ExtendedPropertiesBinder<?, ?, ?> binder =
					(ExtendedPropertiesBinder<?, ?, ?>)this.binderFactory.getBinder(
						binderConfigurationName, DirectWithAttributesChannel.class);

				KafkaConsumerProperties extension = (KafkaConsumerProperties)binder.getExtendedConsumerProperties(it);
				if (extension.isEnableDlq()) {
					String dlqTopic;
					if (extension.getDlqName() != null) {
						dlqTopic = extension.getDlqName();
					} else {
						dlqTopic = "error." + topic + "." + bindingProperties.getGroup();
					}

					FakeBindingSubscribableChannel dlqChannel = new FakeBindingSubscribableChannel(
						dlqTopic,
						"output",
						false,
						this.kafkaTransactionTemplate != null
					);
					dlqChannel.setBeanName(it + ".dlq");
					inboundDlqChannels.put(it, dlqChannel);
					topicDlqChannels.add(dlqTopic, dlqChannel);
				}
			} else {
				FakeBindingSubscribableChannel outboundChannel = new FakeBindingSubscribableChannel(
					topic,
					"output",
					false,
					this.kafkaTransactionTemplate != null
				);
				outboundChannel.setBeanName(it);
				topicOutboundChannels.add(topic, outboundChannel);
				this.outboundChannels.put(it, outboundChannel);
			}
		});

		// bind channels among the same topics
		topicInboundChannels.forEach((key, inboundChannels) -> {
			List<FakeBindingSubscribableChannel> outboundChannels =
				topicOutboundChannels.getOrDefault(key, Collections.emptyList());

			outboundChannels.forEach(outbound ->
				outbound.subscribe(
					new FakeKafkaBindingMessageHandler(
						outbound.getBeanName(),
						inboundChannels,
						inboundDlqChannels,
						this.kafkaMessageAccumulator,
						this.kafkaTransactionTemplate,
						this.executor
					)
				)
			);

			topicDlqChannels.getOrDefault(key, Collections.emptyList()).forEach(dlq ->
				dlq.subscribe(
					new FakeKafkaBindingMessageHandler(
						null,
						inboundChannels,
						inboundDlqChannels,
						this.kafkaMessageAccumulator,
						this.kafkaTransactionTemplate,
						this.executor
					)
				)
			);
		});

		// If there is no inbound channel, create a virtual inbound channel and bind
		topicOutboundChannels.entrySet().stream()
			.filter(entry -> !topicInboundChannels.containsKey(entry.getKey()))
			.forEach(entry -> entry.getValue().forEach(outbound -> {
				FakeBindingSubscribableChannel virtualInboundChannel = new FakeBindingSubscribableChannel(
					entry.getKey(),
					"input",
					false,
					this.kafkaTransactionTemplate != null
				);
				virtualInboundChannel.setBeanName(
					"virtual-inbound-" + outbound.getBeanName().replace("outbound-", "")
				);

				outbound.subscribe(
					new FakeKafkaBindingMessageHandler(
						outbound.getBeanName(),
						Collections.singletonList(virtualInboundChannel),
						inboundDlqChannels,
						this.kafkaMessageAccumulator,
						this.kafkaTransactionTemplate,
						this.executor
					)
				);
			}));
	}

	@Nullable
	@Override
	public SubscribableChannel getInboundChannel(String channelName) {
		return this.inboundChannels.get(channelName);
	}

	@Nullable
	@Override
	public MessageChannel getOutboundChannel(String channelName) {
		return this.outboundChannels.get(channelName);
	}
}
