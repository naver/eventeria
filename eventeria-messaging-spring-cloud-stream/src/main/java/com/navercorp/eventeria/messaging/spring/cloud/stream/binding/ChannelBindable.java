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

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.cloud.stream.binder.Binding;
import org.springframework.cloud.stream.binding.Bindable;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.messaging.MessageChannel;

public final class ChannelBindable implements Bindable {
	private final Map<String, MessageChannel> outputChannels = new HashMap<>();
	private final Map<String, MessageChannel> inputChannels = new HashMap<>();

	public synchronized void registerOutputChannel(String channelName, MessageChannel outputChannel) {
		this.outputChannels.put(channelName, outputChannel);
	}

	public synchronized void registerInputChannel(String channelName, MessageChannel inputChannel) {
		this.inputChannels.put(channelName, inputChannel);
	}

	@Override
	public synchronized Collection<Binding<Object>> createAndBindOutputs(BindingService adapter) {
		return this.outputChannels.entrySet().stream()
			.map(it -> adapter.bindProducer((Object)it.getValue(), it.getKey()))
			.collect(toList());
	}

	@Override
	public synchronized Collection<Binding<Object>> createAndBindInputs(BindingService adapter) {
		return this.inputChannels.entrySet().stream()
			.flatMap(it -> adapter.bindConsumer((Object)it.getValue(), it.getKey()).stream())
			.collect(toList());
	}

	@Override
	public synchronized void unbindOutputs(BindingService adapter) {
		this.getOutputs().forEach(adapter::unbindProducers);
	}

	@Override
	public synchronized void unbindInputs(BindingService adapter) {
		this.getInputs().forEach(adapter::unbindConsumers);
	}

	@Override
	public synchronized Set<String> getOutputs() {
		return Collections.unmodifiableSet(this.outputChannels.keySet());
	}

	@Override
	public synchronized Set<String> getInputs() {
		return Collections.unmodifiableSet(this.inputChannels.keySet());
	}
}
