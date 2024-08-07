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

package com.navercorp.eventeria.domain.repository;

import java.util.Optional;

import com.navercorp.eventeria.domain.entity.AggregateRoot;

/**
 * Create/Read/Update Repository of {@link AggregateRoot}
 * <p/>
 * Delete operation is not supported by default. If need, override {@link AbstractAggregateRepository#delete}.
 *
 * @see AbstractAggregateRepository
 */
public interface AggregateRepository<T extends AggregateRoot, ID> {
	T save(T aggregateRoot);

	Optional<T> findById(ID sourceId);
}
