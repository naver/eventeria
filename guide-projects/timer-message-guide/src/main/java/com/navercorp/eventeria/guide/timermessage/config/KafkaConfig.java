package com.navercorp.eventeria.guide.timermessage.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

	@Bean
	EmbeddedKafkaHolder embeddedKafkaHolder() {
		return new EmbeddedKafkaHolder();
	}
}
