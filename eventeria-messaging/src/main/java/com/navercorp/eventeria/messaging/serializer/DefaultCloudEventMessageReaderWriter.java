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

package com.navercorp.eventeria.messaging.serializer;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventMessageConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventMessageReaderWriter;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventSerializerDeserializer;

public class DefaultCloudEventMessageReaderWriter implements CloudEventMessageReaderWriter {
	private final CloudEventMessageConverter cloudEventMessageConverter;
	private final CloudEventSerializerDeserializer cloudEventSerializerDeserializer;

	public DefaultCloudEventMessageReaderWriter(
		CloudEventMessageConverter cloudEventMessageConverter,
		CloudEventSerializerDeserializer cloudEventSerializerDeserializer
	) {
		this.cloudEventMessageConverter = cloudEventMessageConverter;
		this.cloudEventSerializerDeserializer = cloudEventSerializerDeserializer;
	}

	@Override
	public Message convert(CloudEvent cloudEvent) {
		return this.cloudEventMessageConverter.convert(cloudEvent);
	}

	@Override
	public CloudEvent convert(Message message) {
		return this.cloudEventMessageConverter.convert(message);
	}

	@Override
	public byte[] serialize(CloudEvent cloudEvent) {
		return this.cloudEventSerializerDeserializer.serialize(cloudEvent);
	}

	@Override
	public CloudEvent deserialize(byte[] message) {
		return this.cloudEventSerializerDeserializer.deserialize(message);
	}
}
