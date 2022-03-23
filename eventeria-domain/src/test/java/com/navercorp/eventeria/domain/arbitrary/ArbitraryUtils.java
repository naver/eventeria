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

package com.navercorp.eventeria.domain.arbitrary;

import static java.util.stream.Collectors.toList;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

public class ArbitraryUtils {
	public static <T> T one(Arbitrary<T> arbitrary) {
		return arbitrary.sample();
	}

	public static <T> List<T> list(Arbitrary<T> arbitrary, int size) {
		return arbitrary.sampleStream()
			.skip(size)
			.collect(toList());
	}

	public static Arbitrary<UUID> uuid() {
		return Arbitraries.randomValue(r -> UUID.randomUUID());
	}

	public static Arbitrary<String> aggregateRootId() {
		return Arbitraries.randomValue(r -> UUID.randomUUID().toString());
	}

	public static Arbitrary<Long> aggregateRootVersion() {
		return Arbitraries.longs()
			.greaterOrEqual(1)
			.lessOrEqual(10000000)  // guard to overflow
			.injectNull(0.1);
	}

	public static Arbitrary<OffsetDateTime> currentTime() {
		return Arbitraries.randomValue(r -> OffsetDateTime.now());
	}
}
