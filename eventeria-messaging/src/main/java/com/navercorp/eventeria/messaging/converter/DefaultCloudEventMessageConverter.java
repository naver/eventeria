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

package com.navercorp.eventeria.messaging.converter;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventMessageConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;

public class DefaultCloudEventMessageConverter implements CloudEventMessageConverter {
	private final MessageToCloudEventConverter messageToCloudEventConverter;
	private final CloudEventToMessageConverter cloudEventToMessageConverter;

	public DefaultCloudEventMessageConverter(
		MessageToCloudEventConverter messageToCloudEventConverter,
		CloudEventToMessageConverter cloudEventToMessageConverter
	) {
		this.messageToCloudEventConverter = messageToCloudEventConverter;
		this.cloudEventToMessageConverter = cloudEventToMessageConverter;
	}

	@Override
	public CloudEvent convert(Message message) {
		return this.messageToCloudEventConverter.convert(message);
	}

	@Override
	public Message convert(CloudEvent cloudEvent) {
		return this.cloudEventToMessageConverter.convert(cloudEvent);
	}
}
