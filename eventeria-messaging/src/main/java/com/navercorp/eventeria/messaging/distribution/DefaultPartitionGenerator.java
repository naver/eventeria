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

package com.navercorp.eventeria.messaging.distribution;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.navercorp.eventeria.messaging.contract.distribution.PartitionGenerator;
import com.navercorp.eventeria.messaging.contract.distribution.PartitionKeyExtractor;

public class DefaultPartitionGenerator implements PartitionGenerator {
	private final PartitionKeyExtractor partitionKeyExtractor;
	private final AtomicInteger roundRobinCounter = new AtomicInteger(0);

	public DefaultPartitionGenerator() {
		this(new DefaultPartitionKeyExtractor());
	}

	public DefaultPartitionGenerator(PartitionKeyExtractor partitionKeyExtractor) {
		this.partitionKeyExtractor = partitionKeyExtractor;
	}

	@Override
	public int partition(Object payload, int numOfPartition, @Nullable Supplier<Integer> fallbackPartition) {
		if (numOfPartition < 1) {
			throw new IllegalArgumentException(
				"numOfPartition must be greater than 0. numOfPartition: " + numOfPartition);
		}

		int hash = this.partitionKeyExtractor.extractKey(payload)
			.map(this::selectPartition)
			.orElseGet(() -> this.fallbackPartition(fallbackPartition));

		return Math.abs(hash) % numOfPartition;
	}

	private int fallbackPartition(@Nullable Supplier<Integer> fallbackPartition) {
		if (fallbackPartition != null) {
			return fallbackPartition.get();
		}
		return this.roundRobinCounter.getAndIncrement();
	}

	private int selectPartition(String key) {
		int hashCode = key.hashCode();
		if (hashCode == Integer.MIN_VALUE) {
			hashCode = 0;
		}
		return Math.abs(hashCode);
	}
}
