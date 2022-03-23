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

import static org.assertj.core.api.BDDAssertions.then;

import java.util.HashMap;
import java.util.Map;

import org.springframework.messaging.MessageHeaders;
import org.springframework.util.MimeType;

import net.jqwik.api.Example;

class CloudEventJsonTypeResolverTest {
	private final CloudEventJsonTypeResolver sut = new CloudEventJsonTypeResolver();

	@Example
	void resolveContentType() {
		// given
		Map<String, Object> headers = new HashMap<>();
		headers.put("content-type", "application/cloudevents+json");
		MessageHeaders messageHeaders = new MessageHeaders(headers);

		// when
		MimeType actual = this.sut.resolve(messageHeaders);

		then(actual.getType()).isEqualTo("application");
		then(actual.getSubtype()).isEqualTo("cloudevents+json");
	}

	@Example
	void resolveNull() {
		then(this.sut.resolve(null)).isNull();
	}

	@Example
	void resolveNotContainsContentType() {
		// given
		Map<String, Object> headers = new HashMap<>();
		MessageHeaders messageHeaders = new MessageHeaders(headers);

		then(this.sut.resolve(messageHeaders)).isNull();
	}

	@Example
	void resolveContentTypeNull() {
		// given
		Map<String, Object> headers = new HashMap<>();
		headers.put("content-type", null);
		MessageHeaders messageHeaders = new MessageHeaders(headers);

		then(this.sut.resolve(messageHeaders)).isNull();
	}
}
