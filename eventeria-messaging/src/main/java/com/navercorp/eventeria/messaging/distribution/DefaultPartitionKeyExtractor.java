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

import java.util.Optional;

import io.cloudevents.CloudEventExtensions;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.distribution.PartitionKeyExtractor;
import com.navercorp.eventeria.messaging.contract.distribution.Partitioned;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;

public class DefaultPartitionKeyExtractor implements PartitionKeyExtractor {
	@Override
	public Optional<String> extractKey(Object payload) {
		if (payload instanceof Partitioned) {
			return Optional.ofNullable(((Partitioned)payload).getPartitionKey());
		}

		if (payload instanceof Message) {
			return Optional.ofNullable(((Message)payload).getSourceId());
		}

		if (payload instanceof CloudEventExtensions) {
			PartitionKeyExtension partitionKeyExtension =
				PartitionKeyExtension.parseExtension((CloudEventExtensions)payload);
			if (partitionKeyExtension == null) {
				return Optional.empty();
			}

			return Optional.ofNullable(partitionKeyExtension.getPartitionKey());
		}

		return Optional.empty();
	}
}
