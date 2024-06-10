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

package com.navercorp.spring.boot.eventeria.initializer;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import com.navercorp.eventeria.domain.entity.AggregateMetaManager;
import com.navercorp.eventeria.messaging.contract.meta.EventeriaMetaManager;
import com.navercorp.eventeria.messaging.contract.meta.EventeriaProperties;

/**
 * Initializes {@link com.navercorp.eventeria.messaging.contract.meta.EventeriaMetaManager}.
 */
public class EventeriaMetaManagerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
	private static final String INITIALIZE_META_MANAGER = "eventeria.meta.initialize";

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		String initialize = applicationContext.getEnvironment().getProperty(INITIALIZE_META_MANAGER);
		if ("false".equals(initialize)) {
			return;
		}

		if (StringUtils.hasLength(EventeriaProperties.getEventBasePackage())) {
			EventeriaMetaManager.eventInitializeIfNeeded();
		}

		if (StringUtils.hasLength(EventeriaProperties.getCommandBasePackage())) {
			EventeriaMetaManager.commandInitializeIfNeeded();
		}

		if (StringUtils.hasLength(EventeriaProperties.getAggregateRootBasePackage())) {
			AggregateMetaManager.initializeIfNeeded();
		}
	}
}
