package com.navercorp.eventeria.guide.boot.config;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.kafka.KafkaException;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.EmbeddedKafkaZKBroker;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class EmbeddedKafkaHolder implements InitializingBean {

	private static final EmbeddedKafkaBroker embeddedKafka = new EmbeddedKafkaZKBroker(1, false)
		.brokerListProperty("spring.kafka.bootstrap-servers");

	private static boolean started;

	private final List<String> topics;

	EmbeddedKafkaHolder(List<String> topics) {
		super();
		this.topics = topics;
	}

	public static EmbeddedKafkaBroker getEmbeddedKafka() {
		if (!started) {
			try {
				embeddedKafka.afterPropertiesSet();
			} catch (Exception e) {
				throw new KafkaException("Embedded broker failed to start", e);
			}
			started = true;
		}
		return embeddedKafka;
	}

	@SuppressFBWarnings(
		value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
		justification = "for test"
	)
	@Override
	public void afterPropertiesSet() {
		embeddedKafka.afterPropertiesSet();
		started = true;

		embeddedKafka.addTopics(
			topics.toArray(String[]::new)
		);
	}
}
