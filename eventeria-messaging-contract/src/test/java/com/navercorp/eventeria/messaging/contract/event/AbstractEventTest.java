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

package com.navercorp.eventeria.messaging.contract.event;

import static com.navercorp.eventeria.messaging.contract.util.TestAssertions.assertCloseNow;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.List;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.Label;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.LongRange;
import net.jqwik.api.constraints.Negative;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.constraints.Size;
import net.jqwik.api.constraints.StringLength;
import net.jqwik.api.constraints.UniqueElements;
import net.jqwik.api.domains.Domain;

import com.navercorp.eventeria.messaging.contract.distribution.Partitioned;
import com.navercorp.eventeria.messaging.contract.fixture.AbstractEventFixtures;
import com.navercorp.eventeria.messaging.contract.fixture.AbstractEventFixtures.TestEvent;
import com.navercorp.eventeria.messaging.contract.fixture.EventRaisableSourceFixtures;
import com.navercorp.eventeria.messaging.contract.fixture.TestEventRaisableSource;
import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;
import com.navercorp.eventeria.messaging.contract.source.RaiseEventHandler;

class AbstractEventTest {
	@Example
	void isAssignableFrom() {
		assertThat(Event.class).isAssignableFrom(AbstractEvent.class);
		assertThat(Partitioned.class).isAssignableFrom(AbstractEvent.class);
		assertThat(RaiseEventHandler.class).isAssignableFrom(AbstractEvent.class);
	}

	@Example
	@Label("NoArgsConstructor 로 객체 생성 시 id 는 null 이 아니다.")
	void noArgsConstructor(@ForAll String name) {
		// when
		AbstractEvent sut = new TestEvent(name);

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
		AbstractEvent sut = new TestEvent(sourceId, name);

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
		AbstractEvent sut = new TestEvent(sourceId, sourceVersion, occurrenceTime, name);

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

		assertThatThrownBy(() -> new TestEvent(null, sourceVersion, OffsetDateTime.now(), name))
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
		AbstractEvent sut = new TestEvent(sourceId, sourceVersion, occurrenceTime, name);

		// then
		assertThat(sut.getId()).isNotNull();
		assertThat(sut.getSourceId()).isEqualTo(sourceId);
		assertThat(sut.getSourceVersion()).isNull();
		assertThat(sut.getOccurrenceTime()).isEqualTo(occurrenceTime);
		assertThat(sut.getPartitionKey()).isEqualTo(sut.getSource().toString());
	}

	@Example
	@Label("sourceId, sourceVersion, occurrenceTime 을 사용한 constructor 에서 sourceVersion 는 0 보다 작을 수 없다.")
	void sourceIdAndVersionConstructorVersionCanNotLessThanOne(
		@ForAll @AlphaChars String sourceId,
		@ForAll @Negative Long sourceVersion,
		@ForAll String name) {

		assertThatThrownBy(() -> new TestEvent(sourceId, sourceVersion, OffsetDateTime.now(), name))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("sourceVersion");
	}

	@Example
	@Label("sourceId, sourceVersion, occurrenceTime 을 사용한 constructor 에서 occurrenceTime 은 null 일 수 없다.")
	void sourceIdAndVersionConstructorOccurrenceTimeCanNotBeNull(
		@ForAll @AlphaChars String sourceId,
		@ForAll @LongRange(min = 0) Long sourceVersion,
		@ForAll String name) {

		assertThatThrownBy(() -> new TestEvent(sourceId, sourceVersion, null, name))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessageContaining("occurrenceTime");
	}

	@Example
	@Domain(EventRaisableSourceFixtures.class)
	@Label("onRaised 가 실행되면, AbstractEvent 에 sourceId, sourceVersion, occurrenceTime 이 셋팅된다.")
	void onRaised(@ForAll EventRaisableSource source) {
		// given
		TestEvent sut = new TestEvent("name");

		// when
		sut.onRaised(source);

		// then
		assertThat(sut.getSourceId()).isEqualTo(source.getId());
		if (source.getVersion() == null) {
			assertThat(sut.getSourceVersion()).isNull();
		} else {
			assertThat(sut.getSourceVersion()).isEqualTo(source.getVersion() + 1);
		}
		assertCloseNow(sut.getOccurrenceTime());
	}

	@Example
	@Domain(AbstractEventFixtures.class)
	@Label("onRaised 메소드에 null 을 넘기면 NullPointerException 이 발생한다.")
	void onRaisedSourceNull(@ForAll TestEvent sut) {
		assertThatThrownBy(() -> sut.onRaised(null))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessage("Source can not be null to execute onRaised.");
	}

	@Example
	@Domain(EventRaisableSourceFixtures.class)
	@Label("AbstractEvent 의 getSourceType 과 onRaised sourceType 이 일치하지 않으면 IllegalArgumentException 이 발생한다.")
	void onRaisedSourceTypeNotEquals(@ForAll EventRaisableSource source) {
		TestEvent sut = new TestEvent("name") {
			@Override
			public String getSourceType() {
				return "different-source-type";
			}
		};

		assertThatThrownBy(() -> sut.onRaised(source))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(source.getClass().getName())
			.hasMessageContaining(sut.getSourceType());
	}

	@Example
	@Label("source 의 id 가 null 이면 NullPointerException 이 발생한다.")
	void onRaisedSourceIdNull(@ForAll @Positive Long version, @ForAll @Positive int amount) {
		TestEvent sut = new TestEvent("name");
		EventRaisableSource source = new TestEventRaisableSource(null, version, amount);
		assertThatThrownBy(() -> sut.onRaised(source))
			.isExactlyInstanceOf(NullPointerException.class)
			.hasMessageContaining("sourceId");
	}

	@Example
	@Label("determineRaisedVersion 의 결과가 0 보다 작으면, IllegalArgumentException 이 발생한다.")
	void onRaisedDetermineRaisedVersionShouldBeGreaterThanZero(
		@ForAll @StringLength(min = 1) String sourceId,
		@ForAll @Negative Long version,
		@ForAll @Positive int amount) {

		TestEvent sut = new TestEvent("name") {
			@Override
			public Long determineRaisedVersion(EventRaisableSource source) {
				return source.getVersion();
			}
		};
		EventRaisableSource source = new TestEventRaisableSource(sourceId, version, amount);
		assertThatThrownBy(() -> sut.onRaised(source))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("sourceVersion");
	}

	@Example
	@Label("AbstractEvent 의 sourceId 와 source 의 Id 가 다르면, IllegalArgumentException 이 발생한다.")
	void onRaisedSourceIdDifferent(
		@ForAll @Size(2) @UniqueElements List<@StringLength(min = 1) String> sourceIds,
		@ForAll @Positive Long version) {

		TestEvent sut = new TestEvent(sourceIds.get(0), "name");
		EventRaisableSource source = new TestEventRaisableSource(sourceIds.get(1), version, 0);
		assertThatThrownBy(() -> sut.onRaised(source))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(sut.getSourceId())
			.hasMessageContaining(source.getId());
	}

	@Example
	@Label("AbstractEvent 의 sourceVersion 과 source 의 version 이 다르면, IllegalArgumentException 이 발생한다.")
	void onRaisedVersionDifferent(
		@ForAll @StringLength(min = 1) String sourceId,
		@ForAll @Positive @LongRange(max = 100) Long version) {
		TestEvent sut = new TestEvent(sourceId, version + 10, OffsetDateTime.now(), "name");
		EventRaisableSource source = new TestEventRaisableSource(sourceId, version, 0);
		assertThatThrownBy(() -> sut.onRaised(source))
			.isExactlyInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(sut.getSourceVersion().toString())
			.hasMessageContaining(String.valueOf(source.getVersion() + 1L));
	}
}
