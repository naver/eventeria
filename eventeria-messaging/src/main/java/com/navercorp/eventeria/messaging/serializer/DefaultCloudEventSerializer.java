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
import io.cloudevents.core.format.EventFormat;
import io.cloudevents.core.message.StructuredMessageWriter;
import io.cloudevents.core.message.impl.GenericStructuredMessageReader;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventSerializerDeserializer;

/**
 * A default implementation of serializer/deserializer between {@link CloudEvent} and byte array.
 */
public class DefaultCloudEventSerializer implements CloudEventSerializerDeserializer {
	private final EventFormat eventFormat;
	private final StructuredMessageWriter<byte[]> structuredMessageWriter;

	public DefaultCloudEventSerializer(EventFormat eventFormat) {
		if (eventFormat == null) {
			throw new NullPointerException("EventFormat can not be null.");
		}
		this.eventFormat = eventFormat;
		this.structuredMessageWriter = (format, value) -> value;
	}

	@Override
	public byte[] serialize(CloudEvent cloudEvent) {
		return GenericStructuredMessageReader.from(cloudEvent, this.eventFormat)
			.read(this.structuredMessageWriter);
	}

	@Override
	public CloudEvent deserialize(byte[] message) {
		return new GenericStructuredMessageReader(this.eventFormat, message).toEvent();
	}
}
