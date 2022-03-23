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

import org.springframework.lang.Nullable;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.ContentTypeResolver;
import org.springframework.util.InvalidMimeTypeException;
import org.springframework.util.MimeType;

public class CloudEventJsonTypeResolver implements ContentTypeResolver {
	@Override
	public MimeType resolve(@Nullable MessageHeaders headers) throws InvalidMimeTypeException {
		if (headers == null) {
			return null;
		}

		Object value = headers.get("content-type");
		if (value == null) {
			return null;
		} else if (value instanceof MimeType) {
			return (MimeType)value;
		} else if (value instanceof  String) {
			return MimeType.valueOf((String)value);
		} else {
			throw new IllegalArgumentException(
				"Unknown type for contentType header value: " + value.getClass()
			);
		}
	}
}
