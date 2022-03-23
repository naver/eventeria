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

package com.navercorp.eventeria.domain.spring.repository;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import javax.annotation.Nullable;

import org.springframework.transaction.support.TransactionTemplate;

import com.navercorp.eventeria.domain.entity.AggregateRoot;
import com.navercorp.eventeria.domain.entity.LogicalDeletable;
import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.channel.MessagePublisher;
import com.navercorp.eventeria.validator.executor.ObjectValidatorExecutor;

public class EventPublishRepositoryTemplate {
	private final MessagePublisher messagePublisher;
	private ObjectValidatorExecutor<Object> objectValidatorExecutor;

	@Nullable
	private TransactionTemplate transactionTemplate;

	public EventPublishRepositoryTemplate(MessagePublisher messagePublisher) {
		this.objectValidatorExecutor = obj -> {
		};
		this.messagePublisher = messagePublisher;
	}

	public <T extends AggregateRoot> T save(T aggregateRoot, UnaryOperator<T> saveOperation) {
		this.objectValidatorExecutor.execute(aggregateRoot);
		return this.transactionTemplate != null
			? this.transactionTemplate.execute(status -> this.doSave(aggregateRoot, saveOperation))
			: this.doSave(aggregateRoot, saveOperation);
	}

	public <T extends AggregateRoot> void delete(T aggregateRoot, Consumer<T> deleteOperation) {
		if (aggregateRoot instanceof LogicalDeletable) {
			LogicalDeletable logicalDeletable = (LogicalDeletable)aggregateRoot;
			if (!logicalDeletable.isDeleted()) {
				logicalDeletable.markDeleted();
			}
		}

		if (this.transactionTemplate != null) {
			this.transactionTemplate.execute(status -> {
				this.doDelete(aggregateRoot, deleteOperation);
				return null;
			});
		} else {
			this.doDelete(aggregateRoot, deleteOperation);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends AggregateRoot> void publishEvents(T aggregateRoot) {
		Iterable<? extends Message> messages = aggregateRoot.events();
		if (messages.iterator().hasNext()) {
			if (this.transactionTemplate != null) {
				this.transactionTemplate.execute(status -> {
					this.messagePublisher.publish((Iterable<Message>)messages);
					return null;
				});
			} else {
				this.messagePublisher.publish((Iterable<Message>)messages);
			}
		}
		aggregateRoot.clearEvents();
	}

	private <T extends AggregateRoot> T doSave(T aggregateRoot, UnaryOperator<T> saveOperation) {
		T saved = saveOperation.apply(aggregateRoot);

		this.doPublishEvents(aggregateRoot);

		return saved;
	}

	private <T extends AggregateRoot> void doDelete(T aggregateRoot, Consumer<T> deleteOperation) {
		deleteOperation.accept(aggregateRoot);
		this.doPublishEvents(aggregateRoot);
	}

	@SuppressWarnings("unchecked")
	private <T extends AggregateRoot> void doPublishEvents(T aggregateRoot) {
		Iterable<? extends Message> messages = aggregateRoot.events();
		this.messagePublisher.publish((Iterable<Message>)messages);
		aggregateRoot.clearEvents();
	}

	public void setObjectValidatorExecutor(ObjectValidatorExecutor<Object> validatorExecutor) {
		this.objectValidatorExecutor = validatorExecutor;
	}

	public void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		this.transactionTemplate = transactionTemplate;
	}
}
