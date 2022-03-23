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

import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.Event;

public class EventeriaMetaManager {
	private static final Logger LOG = LoggerFactory.getLogger(EventeriaMetaManager.class);

	private static final Set<Class<? extends Event>> EVENT_TYPES = new CopyOnWriteArraySet<>();
	private static final Set<Class<? extends Command>> COMMAND_TYPES = new CopyOnWriteArraySet<>();
	private static volatile boolean EVENT_INITIALIZED = false;
	private static volatile boolean COMMAND_INITIALIZED = false;

	public static void eventInitializeIfNeeded() {
		if (EVENT_INITIALIZED) {
			return;
		}

		scanAndInitializeEvent();
	}

	public static void commandInitializeIfNeeded() {
		if (COMMAND_INITIALIZED) {
			return;
		}

		scanAndInitializeCommand();
	}

	public static Set<Class<? extends Event>> getEventTypes() {
		eventInitializeIfNeeded();
		return EVENT_TYPES;
	}

	public static Set<Class<? extends Command>> getCommandTypes() {
		scanAndInitializeCommand();
		return COMMAND_TYPES;
	}

	private static synchronized void scanAndInitializeEvent() {
		if (EVENT_INITIALIZED) {
			return;
		}

		String eventBasePackage = EventeriaProperties.getEventBasePackage();
		Reflections reflections = new Reflections(eventBasePackage);
		Set<Class<? extends Event>> eventTypeSet = reflections.getSubTypesOf(Event.class).stream()
			.filter(type -> !type.isInterface())
			.filter(type -> !Modifier.isAbstract(type.getModifiers()))
			.collect(toSet());

		EVENT_TYPES.addAll(eventTypeSet);
		EVENT_INITIALIZED = true;

		LOG.info("EventeriaMetaManager event initialized. eventBasePackage: {}, Event size: {}",
			eventBasePackage, EVENT_TYPES.size());
	}

	private static synchronized void scanAndInitializeCommand() {
		if (COMMAND_INITIALIZED) {
			return;
		}

		String commandBasePackage = EventeriaProperties.getCommandBasePackage();
		Reflections reflections = new Reflections(commandBasePackage);
		Set<Class<? extends Command>> commandTypeSet = reflections.getSubTypesOf(Command.class).stream()
			.filter(type -> !type.isInterface())
			.filter(type -> !Modifier.isAbstract(type.getModifiers()))
			.collect(toSet());

		COMMAND_TYPES.addAll(commandTypeSet);
		COMMAND_INITIALIZED = true;

		LOG.info("EventeriaMetaManager command initialized. commandBasePackage: {}, Command size: {}.",
			commandBasePackage, COMMAND_TYPES.size());
	}
}
