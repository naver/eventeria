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

public final class AnnotatedAggregateMessageHandler {
	private final AggregateRoot aggregateRoot;
	private final AnnotatedAggregateMetaModel metaModel;

	public AnnotatedAggregateMessageHandler(AggregateRoot aggregateRoot) {
		this.aggregateRoot = aggregateRoot;
		this.metaModel = AggregateMetaManager.findAggregateMetaModel(aggregateRoot.getClass());
		if (this.metaModel == null) {
			throw new IllegalStateException("Can not find AnnotatedAggregateMetaModel."
				+ "Check out annotated @AnnotatedAggregateHandler on AggregateRoot. type: " + aggregateRoot);
		}
	}

	public void handle(DomainEvent domainEvent) {
		this.handle(domainEvent, true);
	}

	public void handle(DomainEvent domainEvent, boolean requiredHandler) {
		if (domainEvent == null) {
			return;
		}

		this.metaModel.invoke(this.aggregateRoot, domainEvent, requiredHandler);
	}

	public void handle(Command command) {
		this.handle(command, true);
	}

	public void handle(Command command, boolean requiredHandler) {
		if (command == null) {
			return;
		}

		this.metaModel.invoke(this.aggregateRoot, command, requiredHandler);
	}
}
