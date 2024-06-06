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

package com.navercorp.eventeria.messaging.spring.converter;

import java.util.Arrays;

import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.AbstractMessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventSerializerDeserializer;

/**
 * Implementation of {@link MessageConverter} for {@link CloudEvent}.
 */
public class SpringCloudEventMessageConverter extends AbstractMessageConverter {
	private final CloudEventSerializerDeserializer cloudEventSerializerDeserializer;

	public SpringCloudEventMessageConverter(CloudEventSerializerDeserializer cloudEventSerializerDeserializer) {
		super(
			Arrays.asList(
				new MimeType("application", "cloudevents+json"),
				MimeTypeUtils.APPLICATION_JSON
			)
		);
		this.setContentTypeResolver(new CloudEventJsonTypeResolver());
		this.setStrictContentTypeMatch(true);
		this.cloudEventSerializerDeserializer = cloudEventSerializerDeserializer;
	}

	@Override
	protected boolean supports(Class<?> clazz) {
		return CloudEvent.class.isAssignableFrom(clazz);
	}

	@Nullable
	@Override
	protected Object convertToInternal(
		Object payload, @Nullable MessageHeaders headers, @Nullable Object conversionHint) {

		if (!CloudEvent.class.isAssignableFrom(payload.getClass())) {
			return null;
		}

		return this.cloudEventSerializerDeserializer.serialize((CloudEvent)payload);
	}

	@Nullable
	protected Object convertFromInternal(
		Message<?> message, Class<?> targetClass, @Nullable Object conversionHint) {

		if (byte[].class != message.getPayload().getClass()) {
			return null;
		}

		if (!CloudEvent.class.isAssignableFrom(targetClass)) {
			return null;
		}

		return this.cloudEventSerializerDeserializer.deserialize((byte[])message.getPayload());
	}
}
