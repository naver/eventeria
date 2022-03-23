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

package com.navercorp.eventeria.domain.spring.entity;

import org.springframework.data.annotation.Transient;

import com.navercorp.eventeria.domain.entity.AnnotatedAggregateMessageHandler;
import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.DomainEvent;

public abstract class AnnotatedSpringMessageHandlerAggregateRoot extends SpringMessageHandlerAggregateRoot {
	@Transient
	private final transient AnnotatedAggregateMessageHandler messageHandler = new AnnotatedAggregateMessageHandler(
		this);

	@Override
	protected void handleCommand(Command command) {
		this.messageHandler.handle(command, this.isRequiredCommandHandler());
	}

	@Override
	protected void handleDomainEvent(DomainEvent domainEvent) {
		this.messageHandler.handle(domainEvent, this.isRequiredDomainEventHandler());
	}

	protected boolean isRequiredCommandHandler() {
		return false;
	}

	protected boolean isRequiredDomainEventHandler() {
		return false;
	}
}
