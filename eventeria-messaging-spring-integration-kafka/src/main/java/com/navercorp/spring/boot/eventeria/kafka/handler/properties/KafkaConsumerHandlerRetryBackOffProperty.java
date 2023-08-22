package com.navercorp.spring.boot.eventeria.kafka.handler.properties;

public class KafkaConsumerHandlerRetryBackOffProperty {
	public static KafkaConsumerHandlerRetryBackOffProperty DEFAULT_RETRY_BACKOFF_PROPERTY =
		new KafkaConsumerHandlerRetryBackOffProperty();

	private Long interval = 2000L;
	private Long maxInterval = 2000L;
	private Double multiplier = 1.0;

	public Long getInterval() {
		return interval;
	}

	public void setInterval(Long interval) {
		this.interval = interval;
	}

	public Long getMaxInterval() {
		return maxInterval;
	}

	public void setMaxInterval(Long maxInterval) {
		this.maxInterval = maxInterval;
	}

	public Double getMultiplier() {
		return multiplier;
	}

	public void setMultiplier(Double multiplier) {
		this.multiplier = multiplier;
	}

	@Override
	public String toString() {
		return "KafkaConsumerHandlerRetryBackOffProperty{" +
			"interval=" + interval +
			", maxInterval=" + maxInterval +
			", multiplier=" + multiplier +
			'}';
	}
}
