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

import com.navercorp.eventeria.domain.annotation.AnnotatedAggregateHandler;
import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.DomainEvent;

/**
 * An extended implementation to support managing
 * {@link com.navercorp.eventeria.messaging.contract.Message} changes by aggregate root. <br/>
 * This also supports executions of annotated methods.
 *
 * @see com.navercorp.eventeria.domain.annotation.CommandHandler
 * @see com.navercorp.eventeria.domain.annotation.DomainEventHandler
 */
@AnnotatedAggregateHandler
public abstract class AnnotatedMessageHandlerAggregateRoot extends MessageHandlerAggregateRoot {
	private final transient AnnotatedAggregateMessageHandler<AnnotatedMessageHandlerAggregateRoot>
		messageHandler = new AnnotatedAggregateMessageHandler<>(this);

	@Override
	protected void handleCommand(Command command) {
		this.messageHandler.handle(command, this.isRequiredCommandHandler());
	}

	@Override
	protected void handleDomainEvent(DomainEvent domainEvent) {
		this.messageHandler.handle(domainEvent, this.isRequiredDomainEventHandler());
	}

	/**
	 * Whether handler method of {@link Command} must exist.
	 */
	protected boolean isRequiredCommandHandler() {
		return false;
	}

	/**
	 * Whether handler method of {@link DomainEvent} must exist.
	 */
	protected boolean isRequiredDomainEventHandler() {
		return false;
	}
}
