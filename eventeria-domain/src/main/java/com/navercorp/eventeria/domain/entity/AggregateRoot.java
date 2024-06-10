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

import javax.annotation.Nullable;

import com.navercorp.eventeria.messaging.contract.event.Event;

/**
 * Indicates aggregate root
 *
 * @see <a href="https://martinfowler.com/bliki/DDD_Aggregate.html">DDD Aggregate</a>
 */
public interface AggregateRoot {
	String getId();

	/**
	 * optional: for optimistic locking
	 */
	@Nullable
	Long getVersion();

	/**
	 * @return the pending events raised by aggregate.
	 */
	Iterable<Event> events();

	/**
	 * remove all pending events of aggregate
	 */
	void clearEvents();
}
