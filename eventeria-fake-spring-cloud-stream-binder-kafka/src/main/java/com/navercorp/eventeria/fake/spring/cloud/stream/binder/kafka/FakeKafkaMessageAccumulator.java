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

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventMessageReaderWriter;

public class FakeKafkaMessageAccumulator {
	@Nullable
	private final CloudEventMessageReaderWriter cloudEventMessageReaderWriter;

	private final MultiValueMap<String, Object> published = new LinkedMultiValueMap<>();
	private final MultiValueMap<String, Object> consumed = new LinkedMultiValueMap<>();
	private final MultiValueMap<String, Object> inboundDlq = new LinkedMultiValueMap<>();

	private final List<Runnable> flushTriggers = new CopyOnWriteArrayList<>();

	public FakeKafkaMessageAccumulator() {
		this.cloudEventMessageReaderWriter = null;
	}

	public FakeKafkaMessageAccumulator(CloudEventMessageReaderWriter cloudEventMessageReaderWriter) {
		this.cloudEventMessageReaderWriter = cloudEventMessageReaderWriter;
	}

	public void published(String channel, Object value) {
		this.published.add(channel, value);
	}

	public void consumed(String channel, Object value) {
		this.consumed.add(channel, value);
	}

	public void inboundDlq(String channel, Object value) {
		this.inboundDlq.add(channel, value);
	}

	public List<Object> getPublished(String channel) {
		return this.published.getOrDefault(channel, Collections.emptyList());
	}

	public List<Object> popPublished(String channel) {
		List<Object> values = this.getPublished(channel);
		this.published.clear();
		return values;
	}

	public List<CloudEvent> getPublishedCloudEvents(String channel) {
		return this.getPublished(channel).stream()
			.map(it -> {
				if (it instanceof CloudEvent) {
					return (CloudEvent)it;
				} else if (it.getClass() == byte[].class) {
					Assert.notNull(
						this.cloudEventMessageReaderWriter,
						"cloudEventMessageReaderWriter can not be null for CloudEvent."
					);
					return this.cloudEventMessageReaderWriter.deserialize((byte[])it);
				}

				throw new IllegalArgumentException(
					"published message is not cloudEvent. channel: " + channel + ", message: " + it
				);
			})
			.collect(toList());
	}

	public List<CloudEvent> popPublishedCloudEvents(String channel) {
		List<CloudEvent> cloudEvent = this.getPublishedCloudEvents(channel);
		this.published.clear();
		return cloudEvent;
	}

	public List<Message> getPublishedMessages(String channel) {
		Assert.notNull(
			this.cloudEventMessageReaderWriter,
			"cloudEventMessageReaderWriter can not be null for Message."
		);
		return this.getPublishedCloudEvents(channel).stream()
			.map(this.cloudEventMessageReaderWriter::convert)
			.collect(toList());
	}

	public List<Message> popPublishedMessages(String channel) {
		List<Message> messages = this.getPublishedMessages(channel);
		this.published.clear();
		return messages;
	}

	public List<Object> getConsumed(String channel) {
		return this.consumed.getOrDefault(channel, Collections.emptyList());
	}

	public List<Object> popConsumed(String channel) {
		List<Object> values = this.getConsumed(channel);
		this.consumed.clear();
		return values;
	}

	public List<CloudEvent> getConsumedCloudEvents(String channel) {
		return this.getConsumed(channel).stream()
			.map(it -> {
				if (it instanceof CloudEvent) {
					return (CloudEvent)it;
				} else if (it.getClass() == byte[].class) {
					Assert.notNull(
						this.cloudEventMessageReaderWriter,
						"cloudEventMessageReaderWriter can not be null for CloudEvent."
					);
					return this.cloudEventMessageReaderWriter.deserialize((byte[])it);
				}

				throw new IllegalArgumentException(
					"consumed message is not cloudEvent. channel: " + channel + ", message: " + it
				);
			})
			.collect(toList());
	}

	public List<CloudEvent> popConsumedCloudEvents(String channel) {
		List<CloudEvent> cloudEvent = this.getConsumedCloudEvents(channel);
		this.consumed.clear();
		return cloudEvent;
	}

	public List<Message> getConsumedMessages(String channel) {
		Assert.notNull(
			this.cloudEventMessageReaderWriter,
			"cloudEventMessageReaderWriter can not be null for Message."
		);
		return this.getConsumedCloudEvents(channel).stream()
			.map(this.cloudEventMessageReaderWriter::convert)
			.collect(toList());
	}

	public List<Message> popConsumedMessages(String channel) {
		List<Message> messages = this.getConsumedMessages(channel);
		this.consumed.clear();
		return messages;
	}

	public List<Object> getInboundDlq(String channel) {
		return this.inboundDlq.getOrDefault(channel, Collections.emptyList());
	}

	public List<Object> popInboundDlq(String channel) {
		List<Object> values = this.getInboundDlq(channel);
		this.inboundDlq.clear();
		return values;
	}

	public List<CloudEvent> getInboundDqlCloudEvents(String channel) {
		return this.getInboundDlq(channel).stream()
			.map(it -> {
				if (it instanceof CloudEvent) {
					return (CloudEvent)it;
				} else if (it.getClass() == byte[].class) {
					Assert.notNull(
						this.cloudEventMessageReaderWriter,
						"cloudEventMessageReaderWriter can not be null for CloudEvent."
					);
					return this.cloudEventMessageReaderWriter.deserialize((byte[])it);
				}

				throw new IllegalArgumentException(
					"inboundDlq message is not cloudEvent. channel: " + channel + ", message: " + it
				);
			})
			.collect(toList());
	}

	public List<CloudEvent> popInboundDlqCloudEvents(String channel) {
		List<CloudEvent> cloudEvent = this.getInboundDqlCloudEvents(channel);
		this.inboundDlq.clear();
		return cloudEvent;
	}

	public List<Message> getInboundDlqMessages(String channel) {
		Assert.notNull(
			this.cloudEventMessageReaderWriter,
			"cloudEventMessageReaderWriter can not be null for Message."
		);
		return this.getInboundDqlCloudEvents(channel).stream()
			.map(this.cloudEventMessageReaderWriter::convert)
			.collect(toList());
	}

	public List<Message> popInboundDlqMessages(String channel) {
		List<Message> messages = this.getInboundDlqMessages(channel);
		this.inboundDlq.clear();
		return messages;
	}

	public void registerFlushTrigger(Runnable runnable) {
		this.flushTriggers.add(runnable);
	}

	public void flushTriggers() {
		List<Runnable> triggers = new ArrayList<>(this.flushTriggers);
		this.flushTriggers.clear();

		triggers.forEach(Runnable::run);

		if (!this.flushTriggers.isEmpty()) {
			this.flushTriggers();
		}
	}

	public void flushWith(Runnable runnable) {
		runnable.run();
		this.flushTriggers();
	}

	public void clear() {
		this.published.clear();
		this.consumed.clear();
		this.inboundDlq.clear();
		this.flushTriggers.clear();
	}
}
