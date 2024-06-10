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

package com.navercorp.eventeria.domain.entity;

import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.DomainEvent;

/**
 * Executes annotated {@link Command}/{@link DomainEvent} handler methods.
 *
 * @see com.navercorp.eventeria.domain.annotation.CommandHandler
 * @see com.navercorp.eventeria.domain.annotation.DomainEventHandler
 */
public final class AnnotatedAggregateMessageHandler<T extends AggregateRoot> {
	private final T aggregateRoot;
	private final AnnotatedAggregateMetaModel<T> metaModel;

	@SuppressWarnings("unchecked")
	public AnnotatedAggregateMessageHandler(T aggregateRoot) {
		this.aggregateRoot = aggregateRoot;
		this.metaModel = AggregateMetaManager.findAggregateMetaModel((Class<T>)aggregateRoot.getClass());
		if (this.metaModel == null) {
			throw new IllegalStateException("Can not find AnnotatedAggregateMetaModel."
				+ "Check out annotated @AnnotatedAggregateHandler on AggregateRoot. type: " + aggregateRoot);
		}
	}

	public void handle(DomainEvent domainEvent) {
		this.handle(domainEvent, true);
	}

	/**
	 * invoke handler method of {@link DomainEvent}.
	 * 
	 * @param domainEvent
	 * @param requiredHandler whether handler method must exist.
	 * @throws RequiredDomainEventHandlerException if requiredHandler is true and actual handler method does not exist
	 */
	public void handle(DomainEvent domainEvent, boolean requiredHandler) {
		if (domainEvent == null) {
			return;
		}

		this.metaModel.invoke(this.aggregateRoot, domainEvent, requiredHandler);
	}

	public void handle(Command command) {
		this.handle(command, true);
	}

	/**
	 * invoke handler method of {@link Command}.
	 *
	 * @param command
	 * @param requiredHandler whether handler method must exist.
	 * @throws RequiredCommandHandlerException if requiredHandler is true and actual handler method does not exist
	 */
	public void handle(Command command, boolean requiredHandler) {
		if (command == null) {
			return;
		}

		this.metaModel.invoke(this.aggregateRoot, command, requiredHandler);
	}
}
