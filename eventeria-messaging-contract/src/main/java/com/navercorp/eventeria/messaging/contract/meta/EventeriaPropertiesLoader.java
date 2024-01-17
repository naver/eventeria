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

package com.navercorp.eventeria.messaging.contract.meta;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventeriaPropertiesLoader {
	private static final Logger LOG = LoggerFactory.getLogger(EventeriaPropertiesLoader.class);
	private static final String EVENTERIA_META_INF_PROPERTIES = "/META-INF/eventeria.properties";

	public static Properties load() {
		Properties properties = new Properties();
		try (InputStream propertiesIs = EventeriaPropertiesLoader.class.getResourceAsStream(EVENTERIA_META_INF_PROPERTIES)) {
			if (propertiesIs != null) {
				properties.load(propertiesIs);
				LOG.info("Load {}. {}", EVENTERIA_META_INF_PROPERTIES, properties);
			}
		} catch (IOException e) {
			LOG.error("Can not load properties from " + EVENTERIA_META_INF_PROPERTIES, e);
		}

		return properties;
	}

	public static boolean exists() {
		return EventeriaPropertiesLoader.class.getResource(EVENTERIA_META_INF_PROPERTIES) != null;
	}
}
