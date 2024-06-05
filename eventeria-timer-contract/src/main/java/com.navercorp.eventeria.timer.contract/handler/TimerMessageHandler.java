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

package com.navercorp.eventeria.timer.contract.handler;

import java.util.function.Consumer;

/**
 * A receiving channel of timer messages.
 */
public interface TimerMessageHandler {
	/**
	 * Returns whether a message is {@link com.navercorp.eventeria.timer.contract.TimerMessage}
	 *
	 * @param message
	 * @return true if {@link com.navercorp.eventeria.timer.contract.TimerMessage}
	 */
	boolean isTimerMessage(Object message);

	/**
	 * Register a message into delayed queue.
	 *
	 * @param message a message to be scheduled
	 * @return the identifier value of scheduled message.
	 */
	String register(Object message);

	/**
	 * Retrieve all messages with a scheduled time before this operation is called
	 *
	 * @param consumeReleasedMessage behavior per popped message.
	 */
	void releaseMessages(Consumer<Object> consumeReleasedMessage);

	long getDelayedMessageCount();

	/**
	 * Cancel a scheduled message.
	 * 
	 * @param registeredId the identifier value that created in {@link #register}
	 */
	void cancel(String registeredId);
}
