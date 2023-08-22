package com.navercorp.spring.boot.eventeria.kafka.handler.properties;

import java.util.Map;
import java.util.Optional;

public class KafkaConsumerHandlerPropertySelector {

	public static Optional<KafkaConsumerHandlerProperty> getTargetProperty(
		Map<String, KafkaConsumerHandlerProperty> properties,
		String targetDestination,
		String targetConsumerGroup
	) {
		for (KafkaConsumerHandlerProperty property : properties.values()) {
			if (isTargetProperty(property, targetDestination, targetConsumerGroup)) {
				return Optional.of(property);
			}
		}

		return Optional.empty();
	}

	@SuppressWarnings("RedundantIfStatement")
	private static boolean isTargetProperty(
		KafkaConsumerHandlerProperty property,
		String targetDestination,
		String targetConsumerGroup
	) {
		if (property.getDestinations().contains(targetDestination) &&
			property.getConsumerGroup().contains(targetConsumerGroup)
		) {
			return true;
		} else if (property.getDestinations().contains(targetDestination) &&
			property.getConsumerGroup().isEmpty()
		) {
			return true;
		} else if (property.getDestinations().isEmpty() &&
			property.getConsumerGroup().contains(targetConsumerGroup)
		) {
			return true;
		}

		return false;
	}
}
