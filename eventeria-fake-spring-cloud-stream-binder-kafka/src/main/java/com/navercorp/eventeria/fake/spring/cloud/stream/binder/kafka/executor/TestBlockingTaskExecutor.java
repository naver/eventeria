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

package com.navercorp.eventeria.fake.spring.cloud.stream.binder.kafka.executor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;

/**
 * TaskExecutor executes Runnable with blocking, in order to await for asynchronous call tests in a test environment.
 */
public class TestBlockingTaskExecutor implements TaskExecutor, InitializingBean, DisposableBean {
	private final Executor executor;

	public TestBlockingTaskExecutor(Executor executor) {
		this.executor = executor;
	}

	@Override
	public void execute(Runnable task) {
		CompletableFuture.runAsync(task, this.executor).join();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.executor instanceof InitializingBean initializingBean) {
			initializingBean.afterPropertiesSet();
		}
	}

	@Override
	public void destroy() throws Exception {
		if (this.executor instanceof DisposableBean disposableBean) {
			disposableBean.destroy();
		}
	}
}
