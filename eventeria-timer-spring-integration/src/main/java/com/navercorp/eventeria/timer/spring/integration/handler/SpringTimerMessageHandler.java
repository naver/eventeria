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

package com.navercorp.eventeria.timer.spring.integration.handler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import org.springframework.dao.CannotAcquireLockException;
import org.springframework.integration.handler.AbstractReplyProducingMessageHandler;
import org.springframework.integration.handler.DelayHandlerManagement;
import org.springframework.integration.support.locks.DefaultLockRegistry;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import com.navercorp.eventeria.timer.contract.handler.TimerMessageHandler;

public class SpringTimerMessageHandler extends AbstractReplyProducingMessageHandler implements DelayHandlerManagement {
	private final TimerMessageHandler timerMessageHandler;
	private final String lockKey;
	private final int tryLockTimeoutMs;
	private final LockRegistry lockRegistry;

	public SpringTimerMessageHandler(TimerMessageHandler timerMessageHandler) {
		this(timerMessageHandler, "spring-timer-message-schedule-lock", 60_000, new DefaultLockRegistry());
	}

	public SpringTimerMessageHandler(
		TimerMessageHandler timerMessageHandler,
		String lockKey,
		int tryLockTimeoutMs,
		LockRegistry lockRegistry
	) {
		this.timerMessageHandler = timerMessageHandler;
		this.lockKey = lockKey;
		this.tryLockTimeoutMs = tryLockTimeoutMs;
		this.lockRegistry = lockRegistry;
	}

	@Override
	protected Object handleRequestMessage(Message<?> requestMessage) {
		Object payload = requestMessage.getPayload();

		if (!this.timerMessageHandler.isTimerMessage(payload)) {
			return requestMessage;
		}

		this.timerMessageHandler.register(payload);
		return null;
	}

	@Override
	public int getDelayedMessageCount() {
		return Long.valueOf(this.timerMessageHandler.getDelayedMessageCount()).intValue();
	}

	@Override
	public void reschedulePersistedMessages() {
		Lock lock = this.lockRegistry.obtain(this.lockKey);
		boolean acquired = false;
		try {
			acquired = lock.tryLock(this.tryLockTimeoutMs, TimeUnit.MILLISECONDS);
			if (acquired) {
				this.timerMessageHandler.releaseMessages(
					message -> this.sendOutputs(message, new GenericMessage<>(message)));
			} else {
				throw new CannotAcquireLockException(
					"Acquire lock timeout. lockKey: " + lockKey + ", tryLockTimeout: " + this.tryLockTimeoutMs);
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(
				"reschedule persisted timer message execute interrupted. lockKey: " + lockKey + ", tryLockTimeout: "
					+ this.tryLockTimeoutMs, ex);
		} finally {
			if (acquired) {
				try {
					lock.unlock();
				} catch (Exception ex) {
					logger.warn(ex, "unlock is failed.");
				}
			}
		}
	}
}
