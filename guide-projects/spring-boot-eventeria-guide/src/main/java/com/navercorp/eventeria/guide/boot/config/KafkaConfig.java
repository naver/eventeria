package com.navercorp.eventeria.guide.boot.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

	@Bean("topicsMap")
	@ConfigurationProperties(prefix = "topics")
	Map<String, String> topicsMap() {
		return new HashMap<>();
	}

	@Bean
	EmbeddedKafkaHolder embeddedKafkaHolder(
		@Qualifier("topicsMap") Map<String, String> topicsMap
	) {
		return new EmbeddedKafkaHolder(List.copyOf(topicsMap.values()));
	}
}
