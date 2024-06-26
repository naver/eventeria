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

/**
 * property paths for base-packages.
 * (/META-INF/eventeria.properties or spring-framework Environment)
 */
public class EventeriaBasePackagePropertyPath {

	private EventeriaBasePackagePropertyPath() {
		throw new UnsupportedOperationException("This is a constant class and cannot be instantiated");
	}

	public static final String BASE_PACKAGE_PROPERTY_PATH = "eventeria.base-package";
	public static final String AGGREGATE_ROOT_BASE_PACKAGE_PROPERTY_PATH = "eventeria.aggregate-root-base-package";
	public static final String EVENT_BASE_PACKAGE_PROPERTY_PATH = "eventeria.event-base-package";
	public static final String COMMAND_BASE_PACKAGE_PROPERTY_PATH = "eventeria.command-base-package";
}
