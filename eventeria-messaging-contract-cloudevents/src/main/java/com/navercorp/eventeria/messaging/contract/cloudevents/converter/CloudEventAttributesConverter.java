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

package com.navercorp.eventeria.messaging.contract.cloudevents.converter;

import io.cloudevents.CloudEventAttributes;

import com.navercorp.eventeria.messaging.contract.Message;

/**
 * Converts {@link Message} to {@link CloudEventAttributes}
 *
 * @see <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#required-attributes">cloudevents required attributes</a>
 * @see <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#optional-attributes">cloudevents optional attributes</a>
 */
@FunctionalInterface
public interface CloudEventAttributesConverter {
	CloudEventAttributes convert(Message message);
}
