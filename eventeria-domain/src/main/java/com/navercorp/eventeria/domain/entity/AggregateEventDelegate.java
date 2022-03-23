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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.navercorp.eventeria.messaging.contract.event.Event;
import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;
import com.navercorp.eventeria.messaging.contract.source.RaiseEventHandler;

public final class AggregateEventDelegate {
	private final AggregateRoot aggregateRoot;
	private final List<Event> pendingEvents = new ArrayList<>();

	public AggregateEventDelegate(AggregateRoot aggregateRoot) {
		this.aggregateRoot = aggregateRoot;
	}

	public Iterable<Event> events() {
		if (this.pendingEvents.isEmpty()) {
			return Collections.emptyList();
		}

		List<Event> events = new ArrayList<>(this.pendingEvents);
		return Collections.unmodifiableList(events);
	}

	public void clearEvents() {
		this.pendingEvents.clear();
	}

	public void raiseEvent(Event event) {
		Objects.requireNonNull(event, "The parameter 'event' can not be null.");
		if (!this.aggregateRoot.getClass().getName().equals(event.getSourceType())) {
			throw new IllegalArgumentException("Raise event for compatible sourceType."
				+ " AggregateRoot: " + this.aggregateRoot.getClass().getName()
				+ " Event source type: " + event.getSourceType());
		}

		if (this.aggregateRoot instanceof EventRaisableSource
			&& event instanceof RaiseEventHandler
		) {
			((RaiseEventHandler)event).onRaised((EventRaisableSource)this.aggregateRoot);
		}

		this.pendingEvents.add(event);
	}
}
