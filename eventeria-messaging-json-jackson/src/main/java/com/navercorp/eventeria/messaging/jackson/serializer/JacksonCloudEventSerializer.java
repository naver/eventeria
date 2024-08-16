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

package com.navercorp.eventeria.messaging.jackson.serializer;

import io.cloudevents.core.format.ContentType;
import io.cloudevents.core.provider.EventFormatProvider;

import com.navercorp.eventeria.messaging.serializer.DefaultCloudEventSerializer;

/**
 * A implementation of serializer/deserializer between {@link io.cloudevents.CloudEvent} and byte array<br/>
 * using {@link ContentType#JSON}.
 */
public final class JacksonCloudEventSerializer extends DefaultCloudEventSerializer {
	public JacksonCloudEventSerializer() {
		super(EventFormatProvider.getInstance().resolveFormat(ContentType.JSON));
	}
}
