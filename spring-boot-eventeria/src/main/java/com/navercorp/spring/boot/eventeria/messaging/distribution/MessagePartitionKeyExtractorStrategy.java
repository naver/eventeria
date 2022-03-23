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

import javax.annotation.Nullable;

import org.springframework.cloud.stream.binder.PartitionKeyExtractorStrategy;
import org.springframework.messaging.Message;

import com.navercorp.eventeria.messaging.contract.distribution.PartitionKeyExtractor;
import com.navercorp.eventeria.messaging.distribution.DefaultPartitionKeyExtractor;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;

public class MessagePartitionKeyExtractorStrategy implements PartitionKeyExtractorStrategy {
	private final PartitionKeyExtractor partitionKeyExtractor;

	public MessagePartitionKeyExtractorStrategy() {
		this(new DefaultPartitionKeyExtractor());
	}

	public MessagePartitionKeyExtractorStrategy(PartitionKeyExtractor partitionKeyExtractor) {
		this.partitionKeyExtractor = partitionKeyExtractor;
	}

	@Nullable
	@Override
	public Object extractKey(Message<?> message) {
		return this.partitionKeyExtractor.extractKey(message.getPayload())
			.orElseGet(() -> message.getHeaders()
				.get("ce_" + PartitionKeyExtension.PARTITION_KEY_EXTENSION, String.class)
			);
	}
}
