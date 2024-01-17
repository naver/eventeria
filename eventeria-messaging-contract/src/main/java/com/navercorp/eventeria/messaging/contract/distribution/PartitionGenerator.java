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

package com.navercorp.eventeria.messaging.contract.distribution;

import java.util.function.Supplier;

import javax.annotation.Nullable;

/**
 * determines which partitions to publish
 */
@FunctionalInterface
public interface PartitionGenerator {
	/**
	 * @param payload a message to publish
	 * @param numOfPartition a total partition count
	 * @param fallbackPartition partition supplier to use when failed to extract partition key from payload
	 * @return a partition to be published
	 */
	int partition(Object payload, int numOfPartition, @Nullable Supplier<Integer> fallbackPartition);

	default int partition(Object payload, int numOfPartition) {
		return this.partition(payload, numOfPartition, null);
	}
}
