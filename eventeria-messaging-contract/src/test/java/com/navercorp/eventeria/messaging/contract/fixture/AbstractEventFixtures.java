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

package com.navercorp.eventeria.messaging.contract.fixture;

import java.time.OffsetDateTime;

import javax.annotation.Nullable;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import lombok.Builder;

import com.navercorp.eventeria.messaging.contract.arbitrary.ArbitraryUtils;
import com.navercorp.eventeria.messaging.contract.event.AbstractEvent;
import com.navercorp.eventeria.messaging.contract.fixture.AbstractEventFixtures.TestEvent.TestEventBuilder;
import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;

public class AbstractEventFixtures extends DomainContextBase {

	@Provide
	public Arbitrary<TestEvent> testEvent() {
		return Builders.withBuilder(TestEvent::builder)
			.use(ArbitraryUtils.sourceId()).in(TestEventBuilder::sourceId)
			.use(ArbitraryUtils.sourceVersion()).in(TestEventBuilder::sourceVersion)
			.use(ArbitraryUtils.currentTime()).in(TestEventBuilder::occurrenceTime)
			.use(Arbitraries.strings()).in(TestEventBuilder::name)
			.build(TestEventBuilder::build);
	}

	public static class TestEvent extends AbstractEvent {
		private String name;

		@Builder
		public TestEvent(String sourceId, Long sourceVersion, OffsetDateTime occurrenceTime, String name) {
			super(sourceId, sourceVersion, occurrenceTime);
			this.name = name;
		}

		public TestEvent(String sourceId, String name) {
			super(sourceId);
			this.name = name;
		}

		public TestEvent(String name) {
			this.name = name;
		}

		@Nullable
		@Override
		protected Long determineRaisedVersion(EventRaisableSource source) {
			Long version = source.getVersion();
			return version != null ? version + 1 : null;
		}

		public String getName() {
			return this.name;
		}

		@Override
		public String getSourceType() {
			return TestEventRaisableSource.class.getName();
		}
	}
}
