package com.navercorp.spring.boot.eventeria.kafka.handler.properties;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eventeria.kafka.consumer.error-handler")
public class KafkaConsumerHandlerProperties {
	private static final Logger LOG = LoggerFactory.getLogger(KafkaConsumerHandlerProperties.class);
	private Map<String, KafkaConsumerHandlerProperty> handlers = new HashMap<>();

	@PostConstruct
	public void validate() {
		if (handlers == null || handlers.isEmpty()) {
			return;
		}

		for (KafkaConsumerHandlerProperty property : this.handlers.values()) {
			property.validate();
		}

		if (handlers != null && !handlers.isEmpty()) {
			LOG.info("KafkaConsumerHandlerProperties are registered. {}", handlers);
		}
	}

	public Map<String, KafkaConsumerHandlerProperty> getHandlers() {
		return handlers;
	}

	public void setHandlers(
		Map<String, KafkaConsumerHandlerProperty> handlers
	) {
		this.handlers = handlers;
	}

	@Override
	public String toString() {
		return "KafkaConsumerHandlerProperties{" +
			"handlers=" + handlers +
			'}';
	}
}
