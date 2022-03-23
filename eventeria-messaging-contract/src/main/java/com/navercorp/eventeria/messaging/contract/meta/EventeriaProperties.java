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
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventeriaProperties {
	private static final Logger log = LoggerFactory.getLogger(EventeriaProperties.class);
	private static final String EVENTERIA_META_INF_PROPERTIES = "/META-INF/eventeria.properties";

	private static final Properties PROPERTIES;

	private static String BASE_PACKAGE;
	private static String AGGREGATE_ROOT_BASE_PACKAGE;
	private static String EVENT_BASE_PACKAGE;
	private static String COMMAND_BASE_PACKAGE;

	static {
		Properties properties = new Properties();
		try (InputStream propertiesIs = EventeriaProperties.class.getResourceAsStream(EVENTERIA_META_INF_PROPERTIES)) {
			if (propertiesIs != null) {
				properties.load(propertiesIs);
				log.info("Load {}. {}", EVENTERIA_META_INF_PROPERTIES, properties.toString());
			}
		} catch (IOException e) {
			log.error("Can not load properties from " + EVENTERIA_META_INF_PROPERTIES, e);
		}

		PROPERTIES = properties;
		BASE_PACKAGE = getProperty(EventeriaBasePackagePropertyPath.BASE_PACKAGE_PROPERTY_PATH);
		AGGREGATE_ROOT_BASE_PACKAGE = getProperty(
			EventeriaBasePackagePropertyPath.AGGREGATE_ROOT_BASE_PACKAGE_PROPERTY_PATH);
		EVENT_BASE_PACKAGE = getProperty(EventeriaBasePackagePropertyPath.EVENT_BASE_PACKAGE_PROPERTY_PATH);
		COMMAND_BASE_PACKAGE = getProperty(EventeriaBasePackagePropertyPath.COMMAND_BASE_PACKAGE_PROPERTY_PATH);
	}

	public static String getProperty(String key) {
		String property = System.getProperty(key);
		if (property == null) {
			property = PROPERTIES.getProperty(key);
		}

		return property;
	}

	public static String getBasePackage() {
		return BASE_PACKAGE != null ? BASE_PACKAGE : "";
	}

	public static String getAggregateRootBasePackage() {
		return AGGREGATE_ROOT_BASE_PACKAGE != null ? AGGREGATE_ROOT_BASE_PACKAGE : getBasePackage();
	}

	public static String getEventBasePackage() {
		return EVENT_BASE_PACKAGE != null ? EVENT_BASE_PACKAGE : getBasePackage();
	}

	public static String getCommandBasePackage() {
		return COMMAND_BASE_PACKAGE != null ? COMMAND_BASE_PACKAGE : getBasePackage();
	}
}
