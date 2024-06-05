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

import com.navercorp.eventeria.domain.entity.AggregateEventDelegate;
import com.navercorp.eventeria.domain.entity.AggregateRoot;
import com.navercorp.eventeria.messaging.contract.event.Event;
import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;

/**
 * An abstract implementation provides manipulating (pend/provide/remove) {@link Event}.
 * <p/>
 * This has a same implementation with {@link com.navercorp.eventeria.domain.entity.AbstractAggregateRoot}<br/>
 * except the {@link #eventDelegate} field annotated with {@link Transient}.
 */
public abstract class SpringAggregateRoot implements AggregateRoot, EventRaisableSource {
	@Transient
	private final transient AggregateEventDelegate eventDelegate = new AggregateEventDelegate(this);

	@Override
	public Iterable<Event> events() {
		return this.eventDelegate.events();
	}

	@Override
	public void clearEvents() {
		this.eventDelegate.clearEvents();
	}

	/**
	 * pends {@link Event}
	 *
	 * @param event
	 */
	protected void raiseEvent(Event event) {
		this.eventDelegate.raiseEvent(event);
	}
}
