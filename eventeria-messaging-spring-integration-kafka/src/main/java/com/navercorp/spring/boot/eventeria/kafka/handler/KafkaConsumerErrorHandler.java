package com.navercorp.spring.boot.eventeria.kafka.handler;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import javassist.bytecode.ByteArray;

import com.navercorp.spring.boot.eventeria.kafka.handler.properties.KafkaConsumerHandlerDlqInfo;
import com.navercorp.spring.boot.eventeria.kafka.handler.properties.KafkaConsumerHandlerProperty;

public class KafkaConsumerErrorHandler {
	private final static int INFINITY_RETRIES_VALUE = Integer.MAX_VALUE;

	public static DefaultErrorHandler getErrorHandler(KafkaConsumerHandlerProperty handlerProperty) {
		return switch (handlerProperty.getMode()) {
			case RETRY -> new DefaultErrorHandler(
				getExponentialBackOffWithMaxRetries(handlerProperty, false)
			);

			case INFINITY_RETRY -> new DefaultErrorHandler(
				getExponentialBackOffWithMaxRetries(handlerProperty, true)
			);

			case DLQ -> new DefaultErrorHandler(
				getDeadLetterPublishingRecoverer(handlerProperty),
				getExponentialBackOffWithMaxRetries(handlerProperty, false)
			);
		};
	}

	private static DeadLetterPublishingRecoverer getDeadLetterPublishingRecoverer(
		KafkaConsumerHandlerProperty handlerProperty
	) {
		KafkaConsumerHandlerDlqInfo dlqInfo = handlerProperty.getDlqInfo();
		return new DeadLetterPublishingRecoverer(
			createDlqKafkaTemplate(dlqInfo),
			(cr, e) -> new TopicPartition(dlqInfo.getDestination(), cr.partition() % dlqInfo.getPartition())
		);
	}

	private static ExponentialBackOffWithMaxRetries getExponentialBackOffWithMaxRetries(
		KafkaConsumerHandlerProperty handlerProperty,
		boolean infinityRetry
	) {
		ExponentialBackOffWithMaxRetries backoff;
		if (infinityRetry) {
			backoff = new ExponentialBackOffWithMaxRetries(INFINITY_RETRIES_VALUE);
		} else {
			backoff = new ExponentialBackOffWithMaxRetries(handlerProperty.getMaxRetry());
		}

		backoff.setInitialInterval(handlerProperty.getBackoff().getInterval());
		backoff.setMaxInterval(handlerProperty.getBackoff().getMaxInterval());
		backoff.setMultiplier(handlerProperty.getBackoff().getMultiplier());

		return backoff;
	}

	private static KafkaTemplate<ByteArray, ByteArray> createDlqKafkaTemplate(
		KafkaConsumerHandlerDlqInfo dlqInfo
	) {
		Map<String, Object> properties = new HashMap<>();
		properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, dlqInfo.getBrokers());
		try {
			properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Class.forName(dlqInfo.getKeySerializer()));
			properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, Class.forName(dlqInfo.getValueSerializer()));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("The serializer class does not exist.", e);
		}

		return new KafkaTemplate<>(
			new DefaultKafkaProducerFactory<>(properties)
		);
	}
}
