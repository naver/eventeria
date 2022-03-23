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

package com.navercorp.eventeria.messaging.contract.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.Negative;

import com.navercorp.eventeria.messaging.contract.fixture.AbstractCommandFixtures.TestCommand;

class AbstractCommandTest {
	@Example
	void isAssignableFrom() {
		assertThat(Command.class).isAssignableFrom(AbstractCommand.class);
	}

	@Example
	@Label("NoArgsConstructor 로 객체 생성 시 id, occurrenceTime 은 null 이 아니다.")
	void noArgsConstructor(@ForAll String name) {
		// when
		AbstractCommand sut = new TestCommand(name);

		// then
		assertThat(sut.getId()).isNotNull();
		assertThat(sut.getOccurrenceTime()).isNull();
		assertThat(sut.getSourceId()).isNull();
		assertThat(sut.getSourceVersion()).isNull();
	}

	@Example
	@Label("sourceId constructor 로 생성 시 올바른 값이 설정된다.")
	void sourceIdConstructor(@ForAll @AlphaChars String sourceId, @ForAll String name) {
		// when
		AbstractCommand sut = new TestCommand(sourceId, name);

		// then
		assertThat(sut.getId()).isNotNull();
		assertThat(sut.getSourceId()).isEqualTo(sourceId);
		assertThat(sut.getSourceVersion()).isNull();
		assertThat(sut.getOccurrenceTime()).isNotNull();
		assertThat(sut.getPartitionKey()).isEqualTo(sut.getSource().toString());
	}

	@Example
	@Label("sourceId, sourceVersion, occurrenceTime 을 사용한 constructor 로 생성 시 올바른 값이 설정된다.")
	void sourceIdAndVersionConstructor(
		@ForAll @AlphaChars String sourceId,
		@ForAll @LongRange(min = 0) Long sourceVersion,
		@ForAll String name) {

		// given
		OffsetDateTime occurrenceTime = OffsetDateTime.now();

		// when
		AbstractCommand sut = new TestCommand(sourceId, sourceVersion, occurrenceTime, name);

		// then
		assertThat(sut.getId()).isNotNull();
		assertThat(sut.getSourceId()).isEqualTo(sourceId);
		assertThat(sut.getSourceVersion()).isEqualTo(sourceVersion);
		assertThat(sut.getOccurrenceTime()).isEqualTo(occurrenceTime);
		assertThat(sut.getPartitionKey()).isEqualTo(sut.getSource().toString());
	}

	@Example
	@Label("sourceId, sourceVersion, occurrenceTime 을 사용한 constructor 에서 sourceId 는 null 이 될 수 없다.")
	void sourceIdAndVersionConstructorSourceIdCanNotBeNull(
		@ForAll @LongRange(min = 0) Long sourceVersion,
		@ForAll String name) {

		assertThatThrownBy(
			() -> new TestCommand(null, sourceVersion, OffsetDateTime.now(), name))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessageContaining("sourceId");
	}

	@Example
	@Label("sourceId, sourceVersion, occurrenceTime 을 사용한 constructor 에서 sourceVersion 은 null 을 허용한다.")
	void sourceIdAndVersionConstructorVersionCanBeNull(
		@ForAll @AlphaChars String sourceId,
		@ForAll String name) {

		// given
		Long sourceVersion = null;
		OffsetDateTime occurrenceTime = OffsetDateTime.now();

		// when
		AbstractCommand sut = new TestCommand(sourceId, sourceVersion, occurrenceTime, name);

		// then
		assertThat(sut.getId()).isNotNull();
		assertThat(sut.getSourceId()).isEqualTo(sourceId);
		assertThat(sut.getSourceVersion()).isNull();
		assertThat(sut.getOccurrenceTime()).isEqualTo(occurrenceTime);
		assertThat(sut.getPartitionKey()).isEqualTo(sut.getSource().toString());
	}

	@Example
	@Label("sourceId, sourceVersion, occurrenceTime 을 사용한 constructor 에서 sourceVersion 는 1 보다 작을 수 없다.")
	void sourceIdAndVersionConstructorVersionCanNotLessThanOne(
		@ForAll @AlphaChars String sourceId,
		@ForAll @Negative Long sourceVersion,
		@ForAll String name) {

		assertThatThrownBy(
			() -> new TestCommand(sourceId, sourceVersion, OffsetDateTime.now(), name))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("sourceVersion");
	}

	@Example
	@Label("sourceId, sourceVersion, occurrenceTime 을 사용한 constructor 에서 occurrenceTime 은 null 일 수 없다.")
	void sourceIdAndVersionConstructorOccurrenceTimeCanNotBeNull(
		@ForAll @AlphaChars String sourceId,
		@ForAll @LongRange(min = 0) Long sourceVersion,
		@ForAll String name) {

		assertThatThrownBy(() -> new TestCommand(sourceId, sourceVersion, null, name))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessageContaining("occurrenceTime");
	}
}
