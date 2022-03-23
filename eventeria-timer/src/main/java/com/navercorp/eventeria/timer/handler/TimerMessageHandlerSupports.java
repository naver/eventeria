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

package com.navercorp.eventeria.timer.handler;

import java.time.Instant;
import java.util.Optional;

import com.navercorp.eventeria.timer.contract.TimerMessage;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStoreValue;

class TimerMessageHandlerSupports {
	static Optional<Instant> getReleaseDateTime(Object message) {
		Optional<Instant> timerTime = Optional.empty();

		if (message instanceof TimerMessage) {
			TimerMessage timerMessage = (TimerMessage)message;
			timerTime = timerMessage.timerTime();
		}

		return timerTime;
	}

	static TimerMessageStoreValue toTimerMessageStoreValue(Object message) {
		Instant releaseDateTime = getReleaseDateTime(message).orElseGet(Instant::now);
		return new TimerMessageStoreValue(message, releaseDateTime);
	}
}
