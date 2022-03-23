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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import javax.annotation.Nullable;

import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

public class FakeKafkaBindingMessageHandler extends AggregatingMessageHandler {
	@Nullable
	private final String outboundChannelName;
	private final List<FakeBindingSubscribableChannel> subscribeChannels;
	private final Map<String, FakeBindingSubscribableChannel> subscribeDlqChannels;
	private final FakeKafkaMessageAccumulator kafkaMessageAccumulator;
	@Nullable
	private final TransactionTemplate kafkaTransactionTemplate;
	private final Executor executor;

	public FakeKafkaBindingMessageHandler(
		@Nullable String outboundChannelName,
		List<FakeBindingSubscribableChannel> subscribeChannels,
		Map<String, FakeBindingSubscribableChannel> subscribeDlqChannels,
		FakeKafkaMessageAccumulator kafkaMessageAccumulator,
		@Nullable TransactionTemplate kafkaTransactionTemplate,
		@Nullable Executor executor
	) {
		super(new DefaultAggregatingMessageGroupProcessor());
		super.setExpireGroupsUponCompletion(true);
		this.outboundChannelName = outboundChannelName;
		this.subscribeChannels = subscribeChannels;
		this.subscribeDlqChannels = subscribeDlqChannels;
		this.kafkaMessageAccumulator = kafkaMessageAccumulator;
		this.kafkaTransactionTemplate = kafkaTransactionTemplate;
		this.executor = executor != null
			? executor
			: ForkJoinPool.commonPool();
	}

	@Override
	public void handleMessageInternal(org.springframework.messaging.Message<?> message) {
		// dlq 는 outbound 를 기록하지 않는다.
		if (this.outboundChannelName != null) {
			this.kafkaMessageAccumulator.published(
				this.outboundChannelName,
				message.getPayload()
			);
		}

		super.handleMessageInternal(message);
	}

	@Override
	public void sendOutputs(Object result, org.springframework.messaging.Message<?> requestMessage) {
		org.springframework.messaging.Message<?> message = ((MessageBuilder<?>)result).build();
		if (TransactionSynchronizationManager.isActualTransactionActive()) {
			TransactionSynchronizationManager.registerSynchronization(
				new TransactionSynchronization() {
					@Override
					public void afterCommit() {
						registerFlushTrigger(message);
					}
				}
			);
		} else {
			registerFlushTrigger(message);
		}
	}

	private void registerFlushTrigger(org.springframework.messaging.Message<?> message) {
		this.kafkaMessageAccumulator.registerFlushTrigger(() ->
			this.subscribeChannels.forEach(inbound -> {
				CompletableFuture<?> future = CompletableFuture.runAsync(
					() -> {
						try {
							if (inbound.isTransactional() && this.kafkaTransactionTemplate != null) {
								this.kafkaTransactionTemplate.executeWithoutResult(status ->
									sendToSubscribeChannel(inbound, message)
								);
							} else {
								sendToSubscribeChannel(inbound, message);
							}

							((Collection<?>)message.getPayload()).forEach(it ->
								this.kafkaMessageAccumulator.consumed(
									inbound.getBeanName(),
									it
								)
							);
						} catch (Exception ex) {
							FakeBindingSubscribableChannel dlqChannel = this.subscribeDlqChannels.get(
								inbound.getBeanName());
							if (dlqChannel != null) {
								((Collection<?>)message.getPayload()).forEach(it ->
									this.kafkaMessageAccumulator.inboundDlq(
										inbound.getBeanName(),
										it
									)
								);

								dlqChannel.send(message);
							}
						}
					},
					this.executor
				);

				future.join();
			})
		);
	}

	private void sendToSubscribeChannel(
		FakeBindingSubscribableChannel inbound,
		org.springframework.messaging.Message<?> message
	) {
		if (inbound.isBatchMode()) {
			inbound.send(message);
		} else {
			inbound.send(
				new GenericMessage<>(
					((Iterable<?>)message.getPayload()).iterator().next(),
					message.getHeaders()
				)
			);
		}
	}
}
