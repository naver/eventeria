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

import java.util.Map;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.channel.MessageHandler;
import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.DomainEvent;
import com.navercorp.eventeria.messaging.contract.event.Event;

public abstract class SpringMessageHandlerAggregateRoot extends SpringAggregateRoot implements MessageHandler {
	@Override
	public void handle(Message message, Map<String, Object> headers) {
		if (message instanceof Command) {
			this.handleCommand((Command)message);
		}
		if (message instanceof Event) {
			this.raiseEvent((Event)message);
		}
	}

	protected void raiseEvent(Event event) {
		super.raiseEvent(event);
		if (event instanceof DomainEvent) {
			this.handleDomainEvent((DomainEvent)event);
		}
	}

	protected abstract void handleCommand(Command command);

	protected abstract void handleDomainEvent(DomainEvent domainEvent);
}
