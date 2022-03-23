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

package com.navercorp.eventeria.messaging.contract.channel;

import java.util.Collections;
import java.util.Map;

import com.navercorp.eventeria.messaging.contract.Message;

@FunctionalInterface
public interface MessageHandler {
	default void handle(Message message) {
		this.handle(message, Collections.emptyMap());
	}

	void handle(Message message, Map<String, Object> headers);
}
