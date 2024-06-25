package com.navercorp.spring.boot.eventeria.support;

import java.util.function.Function;

import io.cloudevents.CloudEvent;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventMessageReaderWriter;
import com.navercorp.eventeria.messaging.filter.CloudEventFilter;

/**
 * Utilities for supporting functional binding of spring-cloud-stream
 */
public final class FunctionalBindingSupports {

	private static final CloudEventFilter ACCEPT_ALL_FILTER = cloudEvent -> true;

	private FunctionalBindingSupports() {
		throw new IllegalStateException("utility class");
	}

	public static Function<org.springframework.messaging.Message<byte[]>, Message> convertToMessage(
		CloudEventMessageReaderWriter converter
	) {
		return convertToMessage(converter, ACCEPT_ALL_FILTER);
	}

	public static Function<org.springframework.messaging.Message<byte[]>, Message> convertToMessage(
		CloudEventMessageReaderWriter converter,
		CloudEventFilter cloudEventFilter
	) {
		return message -> {
			CloudEvent cloudEvent = converter.deserialize(message.getPayload());

			if (!cloudEventFilter.accept(cloudEvent)) {
				return null;
			}

			return converter.convert(cloudEvent);
		};
	}
}
