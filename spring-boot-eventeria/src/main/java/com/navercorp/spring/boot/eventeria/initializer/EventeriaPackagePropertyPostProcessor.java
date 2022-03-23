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

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import com.navercorp.eventeria.messaging.contract.meta.EventeriaBasePackagePropertyPath;
import com.navercorp.eventeria.messaging.contract.meta.EventeriaProperties;

public class EventeriaPackagePropertyPostProcessor implements EnvironmentPostProcessor {
	private static final String EVENTERIA_META_INF_PROPERTIES = "/META-INF/eventeria.properties";

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
		String basePackagePath = EventeriaBasePackagePropertyPath.BASE_PACKAGE_PROPERTY_PATH;
		this.checkAndSetProperty(environment, basePackagePath);

		if (!StringUtils.hasLength(System.getProperty(basePackagePath)) && !this.hasMetaFile()) {
			List<String> packages = application.getAllSources().stream()
				.filter(source -> source instanceof Class)
				.map(source -> (Class<?>)source)
				.map(Class::getPackage)
				.map(Package::getName)
				.collect(toList());

			if (packages.size() == 1) {
				System.setProperty(basePackagePath, packages.get(0));
				log.debug("Eventeria base package property set by applicationPackageName. {} : {}", basePackagePath,
					packages.get(0));
			}
		}

		String eventBasePackagePath = EventeriaBasePackagePropertyPath.EVENT_BASE_PACKAGE_PROPERTY_PATH;
		this.checkAndSetProperty(environment, eventBasePackagePath);

		String commandBasePackagePath = EventeriaBasePackagePropertyPath.COMMAND_BASE_PACKAGE_PROPERTY_PATH;
		this.checkAndSetProperty(environment, commandBasePackagePath);

		String aggregateRootBasePackagePath =
			EventeriaBasePackagePropertyPath.AGGREGATE_ROOT_BASE_PACKAGE_PROPERTY_PATH;
		this.checkAndSetProperty(environment, aggregateRootBasePackagePath);
	}

	private void checkAndSetProperty(ConfigurableEnvironment environment, String propertyPath) {
		String basePackage = System.getProperty(propertyPath);
		if (!StringUtils.hasLength(basePackage)) {
			basePackage = environment.getProperty(propertyPath);
			if (StringUtils.hasLength(basePackage)) {
				System.setProperty(propertyPath, basePackage);
			}
		}

		if (StringUtils.hasLength(basePackage)) {
			log.debug("Eventeria package property set. {} : {}", propertyPath, basePackage);
		}
	}

	private boolean hasMetaFile() {
		try (InputStream propertiesIs = EventeriaProperties.class.getResourceAsStream(EVENTERIA_META_INF_PROPERTIES)) {
			return propertiesIs != null;
		} catch (IOException e) {
			return false;
		}
	}
}
