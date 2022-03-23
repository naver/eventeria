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

package com.navercorp.spring.boot.eventeria.messaging.distribution;

import org.apache.kafka.streams.processor.StreamPartitioner;

import com.navercorp.eventeria.messaging.contract.distribution.PartitionGenerator;
import com.navercorp.eventeria.messaging.distribution.DefaultPartitionGenerator;

public class MessageStreamPartitioner<K, V> implements StreamPartitioner<K, V> {
	private final PartitionGenerator partitionGenerator;
	private int partitionCount;

	public MessageStreamPartitioner(int partitionCount) {
		this(new DefaultPartitionGenerator(), partitionCount);
	}

	public MessageStreamPartitioner(PartitionGenerator partitionGenerator, int partitionCount) {
		this.partitionGenerator = partitionGenerator;
		this.partitionCount = partitionCount;
	}

	@Override
	public Integer partition(String topic, Object key, Object value, int numPartitions) {
		return this.partitionGenerator.partition(value, numPartitions);
	}

	public void setPartitionCount(int partitionCount) {
		this.partitionCount = partitionCount;
	}
}
