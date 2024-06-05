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

import io.cloudevents.CloudEventExtensions;

import com.navercorp.eventeria.messaging.contract.Message;

/**
 * Converts {@link Message} to {@link CloudEventExtensions}
 *
 * @see <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/spec.md#extension-context-attributes">cloudevents extensions</a>
 */
@FunctionalInterface
public interface CloudEventExtensionsConverter {
	CloudEventExtensions convert(Message message);
}
