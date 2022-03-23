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

package com.navercorp.eventeria.messaging.contract;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.OffsetDateTime;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.domains.Domain;

import com.navercorp.eventeria.messaging.contract.fixture.AbstractEventFixtures;
import com.navercorp.eventeria.messaging.contract.fixture.AbstractEventFixtures.TestEvent;

class MessageTest {
	@Example
	@Domain(AbstractEventFixtures.class)
	void getSource(@ForAll TestEvent sut) {
		assertThat(sut.getSource()).isEqualTo(URI.create("/" + sut.getSourceType() + "/" + sut.getSourceId()));
	}

	@Example
	@Label("sourceId 가 URI 에 포함될 수 없는 문자열이 포함되도 URLEncoding 을 통해 getSource 를 생성한다.")
	void getSourceSourceIdUrlEncoded() throws UnsupportedEncodingException {
		// given
		String sourceId = "foo bar";
		Message sut = TestEvent.builder()
			.sourceId(sourceId)
			.sourceVersion(0L)
			.occurrenceTime(OffsetDateTime.now())
			.name("name")
			.build();

		// when
		URI actual = sut.getSource();

		assertThat(actual)
			.isEqualTo(URI.create("/" + sut.getSourceType() + "/" + URLEncoder.encode(sourceId, "UTF-8")));
	}
}
