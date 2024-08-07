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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.ReflectionUtils;

import com.navercorp.eventeria.domain.annotation.CommandHandler;
import com.navercorp.eventeria.domain.annotation.DomainEventHandler;
import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.DomainEvent;

/**
 * A metadata model of {@link AnnotatedMessageHandlerAggregateRoot}.
 */
class AnnotatedAggregateMetaModel<T extends AggregateRoot> {
	private final Class<T> aggregateRootType;
	private final Map<Class<? extends Command>, Method> commandHandlerMethods;
	private final Map<Class<? extends DomainEvent>, Method> domainEventHandlerMethods;

	private AnnotatedAggregateMetaModel(
		Class<T> aggregateRootType,
		Map<Class<? extends Command>, Method> commandHandlerMethods,
		Map<Class<? extends DomainEvent>, Method> domainEventHandlerMethods
	) {
		this.aggregateRootType = aggregateRootType;
		this.commandHandlerMethods = Collections.unmodifiableMap(commandHandlerMethods);
		this.domainEventHandlerMethods = Collections.unmodifiableMap(domainEventHandlerMethods);
	}

	static <T extends AggregateRoot> AnnotatedAggregateMetaModel<T> newAggregateMetaModel(Class<T> rootType) {
		Map<Class<? extends Command>, Method> commandHandlerMethods = getCommandHandlerMethods(rootType);
		Map<Class<? extends DomainEvent>, Method> domainEventHandlerMethods = getDomainEventHandlerMethods(rootType);
		return new AnnotatedAggregateMetaModel<>(rootType, commandHandlerMethods, domainEventHandlerMethods);
	}

	/**
	 * build {@link Command} handler method from reflection by finding method annotated with {@link CommandHandler}.
	 */
	private static <T extends AggregateRoot> Map<Class<? extends Command>, Method> getCommandHandlerMethods(
		Class<T> rootType
	) {
		Map<Class<? extends Command>, Method> domainCommandEventHandlerMethods = new HashMap<>();
		Set<Method> commandHandlerMethods = ReflectionUtils.getAllMethods(
			rootType, method -> method.isAnnotationPresent(CommandHandler.class));
		for (Method method : commandHandlerMethods) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes == null || parameterTypes.length == 0) {
				throw new InitializeCommandHandlerException(
					"@CommandHandler method must have Command type parameter at least.");
			}

			Class<?> commandType = parameterTypes[0];
			if (!Command.class.isAssignableFrom(commandType)) {
				throw new InitializeCommandHandlerException(
					"@CommandHandler method's first argument type must be Command type. type: " + commandType);
			}
			if (domainCommandEventHandlerMethods.containsKey(commandType)) {
				Method existMethod = domainCommandEventHandlerMethods.get(commandType);
				if (!existMethod.getName().equals(method.getName())) {
					throw new InitializeCommandHandlerException(
						"Duplicated @CommandHandler for type is detected. commandType: " + commandType);
				}
				if (!existMethod.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())
					&& method.getDeclaringClass().isAssignableFrom(existMethod.getDeclaringClass())) {
					continue;
				}
			}
			method.setAccessible(true);
			domainCommandEventHandlerMethods.put((Class<Command>)commandType, method);
		}

		return domainCommandEventHandlerMethods;
	}

	/**
	 * build {@link DomainEvent} handler method from reflection
	 * by finding method annotated with {@link DomainEventHandler}.
	 */
	private static <T extends AggregateRoot> Map<Class<? extends DomainEvent>, Method> getDomainEventHandlerMethods(
		Class<T> rootType
	) {
		Map<Class<? extends DomainEvent>, Method> domainEventHandlerMethods = new HashMap<>();
		Set<Method> eventHandlerMethods = ReflectionUtils.getAllMethods(
			rootType, method -> method.isAnnotationPresent(DomainEventHandler.class));
		for (Method method : eventHandlerMethods) {
			Class<?>[] parameterTypes = method.getParameterTypes();
			if (parameterTypes == null || parameterTypes.length == 0) {
				throw new InitializeEventHandlerException(
					"@DomainEventHandler method must have DomainEvent type parameter at least.");
			}

			Class<?> eventType = parameterTypes[0];
			if (!DomainEvent.class.isAssignableFrom(eventType)) {
				throw new InitializeEventHandlerException(
					"@DomainEventHandler method's first argument type must be DomainEvent type. type: " + eventType);
			}
			if (domainEventHandlerMethods.containsKey(eventType)) {
				Method existMethod = domainEventHandlerMethods.get(eventType);
				if (!existMethod.getName().equals(method.getName())) {
					throw new InitializeEventHandlerException(
						"Duplicated @DomainEventHandler for type is detected. eventType: " + eventType);
				}
				if (!existMethod.getDeclaringClass().isAssignableFrom(method.getDeclaringClass())
					&& method.getDeclaringClass().isAssignableFrom(existMethod.getDeclaringClass())) {
					continue;
				}
			}
			method.setAccessible(true);
			domainEventHandlerMethods.put((Class<DomainEvent>)eventType, method);
		}

		return domainEventHandlerMethods;
	}

	private static <T extends AggregateRoot, M extends Message> void invokeHandler(
		Method method,
		T aggregateRoot,
		M message
	) {
		try {
			method.invoke(aggregateRoot, message);
		} catch (IllegalAccessException ex) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage());
		} catch (InvocationTargetException ex) {
			Throwable throwable = ex.getTargetException();
			if (throwable instanceof RuntimeException re) {
				throw re;
			}
			if (throwable instanceof Error error) {
				throw error;
			}
			throw new UndeclaredThrowableException(throwable);
		}
	}

	/**
	 * invoke handler method of {@link Command}
	 *
	 * @param aggregateRoot target
	 * @param command
	 * @param required
	 */
	void invoke(T aggregateRoot, Command command, boolean required) {
		if (aggregateRoot.getClass() != this.aggregateRootType) {
			throw new IllegalArgumentException("The argument `aggregateRoot` is not a suitable type for meta models.");
		}

		Method method = this.commandHandlerMethods.get(command.getClass());
		if (method == null) {
			if (required) {
				throw new RequiredCommandHandlerException(
					command,
					"HandlerMethod not found for Command. command: " + command.getClass());
			}
		} else {
			invokeHandler(method, aggregateRoot, command);
		}
	}

	/**
	 * invoke handler method of {@link DomainEvent}
	 *
	 * @param aggregateRoot target
	 * @param domainEvent
	 * @param required
	 */
	void invoke(T aggregateRoot, DomainEvent domainEvent, boolean required) {
		if (aggregateRoot.getClass() != this.aggregateRootType) {
			throw new IllegalArgumentException("The argument `aggregateRoot` is not a suitable type for meta models.");
		}

		Method method = this.domainEventHandlerMethods.get(domainEvent.getClass());
		if (method == null) {
			if (required) {
				throw new RequiredDomainEventHandlerException(
					domainEvent,
					"HandlerMethod not found for domainEvent. domainEvent: " + domainEvent.getClass());
			}
		} else {
			invokeHandler(method, aggregateRoot, domainEvent);
		}
	}
}
