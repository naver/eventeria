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

package com.navercorp.eventeria.messaging.spring.cloud.stream.binding;

import org.springframework.cloud.stream.binding.SubscribableChannelBindingTargetFactory;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public class DefaultChannelBinder implements ChannelBinder {
	private final SubscribableChannelBindingTargetFactory bindingTargetFactory;
	private final ChannelBindable channelBindable;

	public DefaultChannelBinder(
		SubscribableChannelBindingTargetFactory bindingTargetFactory,
		ChannelBindable channelBindable
	) {
		this.bindingTargetFactory = bindingTargetFactory;
		this.channelBindable = channelBindable;
	}

	@Override
	public MessageChannel getOutboundChannel(String channelName) {
		SubscribableChannel outboundChannel = this.bindingTargetFactory.createOutput(channelName);
		this.channelBindable.registerOutputChannel(channelName, outboundChannel);
		return outboundChannel;
	}

	@Override
	public SubscribableChannel getInboundChannel(String channelName) {
		SubscribableChannel inboundChannel = this.bindingTargetFactory.createInput(channelName);
		this.channelBindable.registerInputChannel(channelName, inboundChannel);
		return inboundChannel;
	}
}
