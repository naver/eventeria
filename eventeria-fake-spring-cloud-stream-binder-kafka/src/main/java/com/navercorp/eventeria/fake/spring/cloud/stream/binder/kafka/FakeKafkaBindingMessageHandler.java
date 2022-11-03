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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import javax.annotation.Nullable;

import org.springframework.integration.IntegrationMessageHeaderAccessor;
import org.springframework.integration.aggregator.AggregatingMessageHandler;
import org.springframework.integration.aggregator.CorrelationStrategy;
import org.springframework.integration.aggregator.DefaultAggregatingMessageGroupProcessor;
import org.springframework.integration.aggregator.HeaderAttributeCorrelationStrategy;
import org.springframework.integration.aggregator.ReleaseStrategy;
import org.springframework.integration.aggregator.SimpleSequenceSizeReleaseStrategy;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.listener.BatchListenerFailedException;
import org.springframework.messaging.Message;
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
		super.setCorrelationStrategy(new FakeKafkaCorrelationStrategy());
		super.setReleaseStrategy(new FakeKafkaReleaseStrategy());
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
					() -> executeSubscribe(inbound, message),
					this.executor
				);

				future.join();
			})
		);
	}

	private void executeSubscribe(
		FakeBindingSubscribableChannel inbound,
		org.springframework.messaging.Message<?> message
	) {
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
			BatchListenerFailedException batchException = getBatchListenerFailedException(ex);
			if (batchException != null) {
				List<?> payloads = new ArrayList<>((Collection<?>)message.getPayload());
				if (payloads.isEmpty()) {
					throw ex;
				}

				int errorIndex = batchException.getIndex();

				List<Object> consumedPayloads = new ArrayList<>();
				Object errorPayload = null;
				List<Object> remainPayloads = new ArrayList<>();
				for (int i = 0, size = payloads.size(); i < size; i++) {
					Object payload = payloads.get(i);
					if (i < errorIndex) {
						consumedPayloads.add(payload);
					} else if (i == errorIndex) {
						errorPayload = payload;
					} else {
						remainPayloads.add(payload);
					}
				}

				consumedPayloads.forEach(it -> this.kafkaMessageAccumulator.consumed(inbound.getBeanName(), it));
				if (errorPayload != null) {
					this.sendToDlq(
						inbound,
						new GenericMessage<>(
							Collections.singletonList(errorPayload),
							message.getHeaders()
						)
					);
				}

				if (!remainPayloads.isEmpty()) {
					executeSubscribe(
						inbound,
						new GenericMessage<>(
							remainPayloads,
							message.getHeaders()
						)
					);
				}
			} else {
				this.sendToDlq(inbound, message);
			}
		}
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

	private void sendToDlq(
		FakeBindingSubscribableChannel inbound,
		org.springframework.messaging.Message<?> message
	) {
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

	@org.springframework.lang.Nullable
	private BatchListenerFailedException getBatchListenerFailedException(Throwable throwableArg) {
		if (throwableArg == null || throwableArg instanceof BatchListenerFailedException) {
			return (BatchListenerFailedException) throwableArg;
		}

		BatchListenerFailedException target = null;

		Throwable throwable = throwableArg;
		Set<Throwable> checked = new HashSet<>();
		while (throwable.getCause() != null && !checked.contains(throwable.getCause())) {
			throwable = throwable.getCause();
			checked.add(throwable);

			if (throwable instanceof BatchListenerFailedException) {
				target = (BatchListenerFailedException) throwable;
				break;
			}
		}

		return target;
	}

	private static final class FakeKafkaCorrelationStrategy implements CorrelationStrategy {
		private final CorrelationStrategy delegate = new HeaderAttributeCorrelationStrategy(
			IntegrationMessageHeaderAccessor.CORRELATION_ID
		);

		@Override
		public Object getCorrelationKey(Message<?> message) {
			Object correlationId = this.delegate.getCorrelationKey(message);
			if (correlationId == null) {
				correlationId = UUID.randomUUID();
			}
			return correlationId;
		}
	}

	private static final class FakeKafkaReleaseStrategy implements ReleaseStrategy {
		private final ReleaseStrategy delegate = new SimpleSequenceSizeReleaseStrategy();

		@Override
		public boolean canRelease(MessageGroup group) {
			if (group.size() == 0) {
				return false;
			}

			Object size = group.getOne().getHeaders().get(IntegrationMessageHeaderAccessor.SEQUENCE_SIZE);
			if (size == null) {
				return true;
			}

			return this.delegate.canRelease(group);
		}
	}
}
