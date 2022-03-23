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

package com.navercorp.eventeria.fake.spring.cloud.stream.binder.kafka;

import org.springframework.integration.channel.PublishSubscribeChannel;

public class FakeBindingSubscribableChannel extends PublishSubscribeChannel {
	private final String topic;
	private final String type;
	private final boolean batchMode;
	private final boolean transactional;

	public FakeBindingSubscribableChannel(
		String topic,
		String type,
		boolean batchMode,
		boolean transactional
	) {
		this.topic = topic;
		this.type = type;
		this.batchMode = batchMode;
		this.transactional = transactional;
	}

	public String getTopic() {
		return this.topic;
	}

	public String getType() {
		return this.type;
	}

	public boolean isBatchMode() {
		return this.batchMode;
	}

	public boolean isTransactional() {
		return this.transactional;
	}
}
