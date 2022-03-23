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

package com.navercorp.eventeria.messaging.spring.cloud.stream.support;

import org.springframework.cloud.stream.config.BindingServiceProperties;

public class BindingSupports {
	public static boolean isBatchConsumer(
		BindingServiceProperties bindingServiceProperties,
		String topic,
		String group
	) {
		return bindingServiceProperties.getBindings().values().stream()
			.filter(bindingProperties -> topic.equals(bindingProperties.getDestination()))
			.filter(bindingProperties -> group.equals(bindingProperties.getGroup()))
			.filter(bindingProperties -> bindingProperties.getConsumer() != null)
			.map(bindingProperties -> bindingProperties.getConsumer().isBatchMode())
			.findAny()
			.orElse(false);
	}
}
