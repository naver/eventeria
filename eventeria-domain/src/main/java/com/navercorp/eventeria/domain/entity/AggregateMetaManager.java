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

package com.navercorp.eventeria.domain.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.eventeria.domain.annotation.AnnotatedAggregateHandler;
import com.navercorp.eventeria.messaging.contract.meta.EventeriaProperties;

public final class AggregateMetaManager {
	private static final Logger LOG = LoggerFactory.getLogger(AggregateMetaManager.class);
	private static final Map<Class<? extends AggregateRoot>, AnnotatedAggregateMetaModel> META_MODELS =
		new ConcurrentHashMap<>();
	private static volatile boolean INITIALIZED = false;

	public static void initializeIfNeeded() {
		if (INITIALIZED) {
			return;
		}

		scanAndInitialize();
	}

	@Nullable
	static <T extends AggregateRoot> AnnotatedAggregateMetaModel<T> findAggregateMetaModel(Class<T> aggregateRootType) {
		if (!aggregateRootType.isAnnotationPresent(AnnotatedAggregateHandler.class)) {
			return null;
		}

		return META_MODELS.computeIfAbsent(aggregateRootType, AnnotatedAggregateMetaModel::newAggregateMetaModel);
	}

	@SuppressWarnings("unchecked")
	private static synchronized void scanAndInitialize() {
		if (INITIALIZED) {
			return;
		}

		Map<Class<? extends AggregateRoot>, AnnotatedAggregateMetaModel> metaModelMap = new HashMap<>();
		String aggregateRootBasePackage = EventeriaProperties.getAggregateRootBasePackage();
		Reflections reflections = new Reflections(aggregateRootBasePackage);
		Set<Class<?>> aggregateHandlerTypes = reflections.getTypesAnnotatedWith(AnnotatedAggregateHandler.class, true);

		aggregateHandlerTypes.stream()
			.filter(AggregateRoot.class::isAssignableFrom)
			.filter(handlerType -> !META_MODELS.containsKey(handlerType))
			.forEach(handlerType -> {
				AnnotatedAggregateMetaModel metaModel = AnnotatedAggregateMetaModel.newAggregateMetaModel(
					(Class<AggregateRoot>)handlerType);
				metaModelMap.put((Class<? extends AggregateRoot>)handlerType, metaModel);
			});

		META_MODELS.putAll(metaModelMap);
		INITIALIZED = true;

		LOG.info(
			"AggregateMetaManager initialized. aggregateRootBasePackage: {},"
				+ "AnnotatedAggregateHandler AggregateMetaModel size: {}.",
			aggregateRootBasePackage, META_MODELS.size());
	}
}
