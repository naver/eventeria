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

package com.navercorp.eventeria.timer.store;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.navercorp.eventeria.timer.contract.store.TimerMessageStore;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStoreValue;

public class InMemoryTimerMessageStore implements TimerMessageStore {
	private final Map<Integer, List<TimerMessageStoreValue>> store = new HashMap<>();

	@Override
	public synchronized void save(TimerMessageStoreValue storeValue, @Nullable Integer partition) {
		List<TimerMessageStoreValue> list = this.getList(partition);
		list.add(storeValue);
		list.sort(Comparator.comparing(TimerMessageStoreValue::getReleaseDateTime));
	}

	@Override
	public synchronized void remove(UUID storeValueId, @Nullable Integer partition) {
		List<TimerMessageStoreValue> list = this.getList(partition);
		list.removeIf(it -> it.getId().equals(storeValueId));
	}

	@Override
	public synchronized long count(Instant conditionDateTime, @Nullable Integer partition) {
		return this.getList(partition).stream()
			.filter(it -> it.getReleaseDateTime().isBefore(conditionDateTime))
			.count();
	}

	@Override
	public synchronized List<TimerMessageStoreValue> findReleaseValues(
		Instant conditionDateTime,
		int count,
		@Nullable Integer partition
	) {
		return this.getList(partition).stream()
			.filter(it -> it.getReleaseDateTime().isBefore(conditionDateTime))
			.limit(count)
			.collect(Collectors.toList());
	}

	private List<TimerMessageStoreValue> getList(@Nullable Integer partition) {
		if (partition == null) {
			partition = 0;
		}

		return this.store.getOrDefault(partition, new ArrayList<>());
	}
}
