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

package com.navercorp.eventeria.messaging.contract.event;

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.domains.Domain;

import com.navercorp.eventeria.messaging.contract.fixture.AbstractDomainEventFixtures.TestDomainEvent;
import com.navercorp.eventeria.messaging.contract.fixture.EventRaisableSourceFixtures;
import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;

class AbstractDomainEventTest {
	@Example
	void isAssignableFrom() {
		assertThat(AbstractEvent.class).isAssignableFrom(AbstractDomainEvent.class);
		assertThat(DomainEvent.class).isAssignableFrom(AbstractDomainEvent.class);
	}

	@Example
	@Domain(EventRaisableSourceFixtures.class)
	@Label("AbstractDomainEvent 에 EventRaisableSource 가 raised 되면, Event 의 version 은 +1 이 된다.")
	void onRaisedDetermineEventVersion(@ForAll EventRaisableSource source) {
		// Given
		AbstractDomainEvent sut = new TestDomainEvent();

		// When
		sut.onRaised(source);

		// Then
		if (sut.getSourceVersion() == null) {
			assertThat(source.getVersion()).isNull();
		} else {
			assertThat(sut.getSourceVersion()).isEqualTo(source.getVersion() + 1);
		}
	}
}
