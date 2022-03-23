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

package com.navercorp.eventeria.messaging.fixture;

import java.time.Instant;
import java.util.Map;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.navercorp.eventeria.messaging.contract.event.AbstractDomainEvent;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@ToString
public class TestDomainEvent extends AbstractDomainEvent {
	private String name;

	public TestDomainEvent() {
	}

	public TestDomainEvent(String name) {
		this.name = name;
	}

	@Builder
	public TestDomainEvent(
		String sourceId,
		Long sourceVersion,
		Instant occurrenceTime,
		String name,
		Map<String, Object> extensions
	) {
		super(sourceId, sourceVersion, occurrenceTime);
		this.name = name;
		super.setExtensions(extensions);
	}

	@Override
	public String getSourceType() {
		return "com.navercorp.eventeria.order.Order";
	}
}
