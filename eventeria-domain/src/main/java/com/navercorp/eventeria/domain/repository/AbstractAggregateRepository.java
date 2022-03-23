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

package com.navercorp.eventeria.domain.repository;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import com.navercorp.eventeria.domain.entity.AggregateRoot;
import com.navercorp.eventeria.domain.entity.LogicalDeletable;
import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.channel.MessagePublisher;
import com.navercorp.eventeria.validator.executor.DefaultObjectValidatorExecutor;
import com.navercorp.eventeria.validator.executor.ObjectValidatorExecutor;

public abstract class AbstractAggregateRepository<T extends AggregateRoot, ID> implements AggregateRepository<T, ID> {
	private final ObjectValidatorExecutor<T> objectValidatorExecutor;
	private final MessagePublisher messagePublisher;

	protected AbstractAggregateRepository() {
		this(messages -> {
		});
	}

	protected AbstractAggregateRepository(MessagePublisher messagePublisher) {
		this(new DefaultObjectValidatorExecutor<>(), messagePublisher);
	}

	protected AbstractAggregateRepository(ObjectValidatorExecutor<T> objectValidatorExecutor,
		MessagePublisher messagePublisher) {
		this.objectValidatorExecutor = objectValidatorExecutor;
		this.messagePublisher = messagePublisher;
	}

	protected void validate(T aggregateRoot) {
		this.objectValidatorExecutor.execute(aggregateRoot);
	}

	@SuppressWarnings("unchecked")
	protected void publishEvents(T aggregateRoot) {
		Iterable<? extends Message> messages = aggregateRoot.events();
		this.messagePublisher.publish((Iterable<Message>)messages);
		aggregateRoot.clearEvents();
	}

	protected T saveAndReturn(T aggregateRoot, UnaryOperator<T> persistStore) {
		this.validate(aggregateRoot);
		aggregateRoot = persistStore.apply(aggregateRoot);
		this.publishEvents(aggregateRoot);
		return aggregateRoot;
	}

	protected T save(T aggregateRoot, Consumer<T> persistStore) {
		return this.saveAndReturn(aggregateRoot, instance -> {
			persistStore.accept(instance);
			return instance;
		});
	}

	protected void markDeleted(LogicalDeletable aggregateRoot) {
		if (!aggregateRoot.isDeleted()) {
			aggregateRoot.markDeleted();
		}
	}

	protected void delete(T aggregateRoot, Consumer<T> deleteStore) {
		if (aggregateRoot instanceof LogicalDeletable) {
			this.markDeleted((LogicalDeletable)aggregateRoot);
		}

		deleteStore.accept(aggregateRoot);
		this.publishEvents(aggregateRoot);
	}

	protected ObjectValidatorExecutor<T> getObjectValidatorExecutor() {
		return this.objectValidatorExecutor;
	}

	protected MessagePublisher getMessagePublisher() {
		return this.messagePublisher;
	}
}
