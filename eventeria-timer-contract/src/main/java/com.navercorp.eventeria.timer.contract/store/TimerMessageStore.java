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

package com.navercorp.eventeria.timer.contract.store;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * A store for publishing timer messages.
 */
public interface TimerMessageStore {
	void save(TimerMessageStoreValue storeValue, @Nullable Integer partition);

	void remove(UUID storeValueId, @Nullable Integer partition);

	long count(Instant conditionDateTime, @Nullable Integer partition);

	/**
	 * find all messages by a scheduled time and partitions.
	 *
	 * @param conditionDateTime
	 * @param count
	 * @param partition
	 * @return
	 */
	List<TimerMessageStoreValue> findReleaseValues(Instant conditionDateTime, int count, @Nullable Integer partition);
}
