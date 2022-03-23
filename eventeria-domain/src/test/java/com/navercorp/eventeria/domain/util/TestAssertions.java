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

package com.navercorp.eventeria.domain.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import org.assertj.core.data.TemporalUnitWithinOffset;

public class TestAssertions {
	private static final TemporalUnitWithinOffset INSTANT_CLOSE_TO_OFFSET =
		new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS);

	public static void assertCloseNow(OffsetDateTime offsetDateTime) {
		assertCloseTime(offsetDateTime, OffsetDateTime.now());
	}

	public static void assertCloseTime(OffsetDateTime offsetDateTime, OffsetDateTime close) {
		assertThat(offsetDateTime).isCloseTo(close, TestAssertions.INSTANT_CLOSE_TO_OFFSET);
	}
}
