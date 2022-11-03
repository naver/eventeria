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

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.messaging.support.GenericMessage;

public final class FakeKafkaMessageTrigger {
	private final FakeKafkaChannelBinder binder;

	public FakeKafkaMessageTrigger(FakeKafkaChannelBinder binder) {
		this.binder = binder;
	}

	public void sendOutboundChannelMessage(String channelName, Object message) {
		MessageChannel channel = this.binder.getOutboundChannel(channelName);
		if (channel == null) {
			throw new IllegalArgumentException(
				"channel does not registered for fake kafka binder outbound channel. channelName: " + channelName
			);
		}

		channel.send(new GenericMessage<>(message));
	}

	public void sendInboundChannelMessage(String channelName, Object message) {
		SubscribableChannel channel = this.binder.getInboundChannel(channelName);
		if (channel == null) {
			throw new IllegalArgumentException(
				"channel does not registered for fake kafka binder inbound channel. channelName: " + channelName
			);
		}

		channel.send(new GenericMessage<>(message));
	}
}
