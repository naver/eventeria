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

package com.navercorp.eventeria.fake.spring.cloud.stream.binder.kafka.test;

import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import com.navercorp.eventeria.fake.spring.cloud.stream.binder.kafka.FakeKafkaMessageAccumulator;

public class FakeKafkaMessageAccumulatorClearanceExecutionListener extends AbstractTestExecutionListener {
	@Override
	public void beforeTestMethod(TestContext testContext) {
		this.clear(testContext.getApplicationContext());
	}

	@Override
	public void afterTestMethod(TestContext testContext) {
		this.clear(testContext.getApplicationContext());
	}

	private void clear(ApplicationContext context) {
		context.getBeansOfType(FakeKafkaMessageAccumulator.class).values()
			.forEach(FakeKafkaMessageAccumulator::clear);
	}
}
