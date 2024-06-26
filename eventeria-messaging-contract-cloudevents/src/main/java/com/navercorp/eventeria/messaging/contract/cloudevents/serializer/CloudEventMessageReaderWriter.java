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

package com.navercorp.eventeria.messaging.contract.cloudevents.serializer;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventMessageConverter;

/**
 * deserialize byte array to {@link Message>}, and serialize {@link Message} to byte array.
 */
public interface CloudEventMessageReaderWriter extends CloudEventMessageConverter, CloudEventSerializerDeserializer {
	default byte[] write(Message message) {
		CloudEvent cloudEvent = convert(message);
		return serialize(cloudEvent);
	}

	default Message read(byte[] payload) {
		CloudEvent cloudEvent = deserialize(payload);
		return convert(cloudEvent);
	}
}
