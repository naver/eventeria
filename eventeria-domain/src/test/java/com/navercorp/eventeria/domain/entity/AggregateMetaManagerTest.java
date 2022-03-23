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

import static org.assertj.core.api.Assertions.assertThat;

import net.jqwik.api.Example;
import net.jqwik.api.Label;

import com.navercorp.eventeria.domain.fixture.AbstractAggregateRootFixtures.TestAggregateRoot;
import com.navercorp.eventeria.messaging.contract.event.Event;

class AggregateMetaManagerTest {
	@Example
	@Label("aggregateType 으로 AggregateMetaModel 정보를 조회합니다.")
	void findAggregateMetaModel() {
		// Given
		Class<TestAggregateRoot> aggregateType = TestAggregateRoot.class;

		// When
		AnnotatedAggregateMetaModel<TestAggregateRoot> actual = AggregateMetaManager.findAggregateMetaModel(
			aggregateType);

		// Then
		assertThat(actual).isNotNull();
	}

	@Example
	@Label("@AnnotatedAggregateHandler 가 없는 AggregateRoot 는  AggregateMetaModel 조회시 null 을 반한홥니다.")
	void findAggregateMetaModelNoAggregateMetaModel() {
		// Given
		Class<NoAggregateMetaModelSpec> aggregateType = NoAggregateMetaModelSpec.class;

		// When
		AnnotatedAggregateMetaModel<NoAggregateMetaModelSpec> actual
			= AggregateMetaManager.findAggregateMetaModel(aggregateType);

		// Then
		assertThat(actual).isNull();
	}

	static class NoAggregateMetaModelSpec implements AggregateRoot {
		@Override
		public String getId() {
			return null;
		}

		@Override
		public Long getVersion() {
			return null;
		}

		@Override
		public Iterable<Event> events() {
			return null;
		}

		@Override
		public void clearEvents() {

		}
	}
}
