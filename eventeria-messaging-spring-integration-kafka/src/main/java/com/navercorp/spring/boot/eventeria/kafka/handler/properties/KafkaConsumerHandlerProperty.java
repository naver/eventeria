package com.navercorp.spring.boot.eventeria.kafka.handler.properties;

import java.util.Collections;
import java.util.List;

public class KafkaConsumerHandlerProperty {

	private KafkaConsumerHandlerType mode;
	private List<String> destinations = Collections.emptyList();
	private List<String> consumerGroup = Collections.emptyList();
	private Integer maxRetry;
	private KafkaConsumerHandlerRetryBackOffProperty backoff =
		KafkaConsumerHandlerRetryBackOffProperty.DEFAULT_RETRY_BACKOFF_PROPERTY;
	private KafkaConsumerHandlerDlqInfo dlqInfo;

	public KafkaConsumerHandlerType getMode() {
		return mode;
	}

	public void setMode(
		KafkaConsumerHandlerType mode) {
		this.mode = mode;
	}

	public List<String> getDestinations() {
		return destinations;
	}

	public void setDestinations(List<String> destinations) {
		this.destinations = destinations;
	}

	public List<String> getConsumerGroup() {
		return consumerGroup;
	}

	public void setConsumerGroup(List<String> consumerGroup) {
		this.consumerGroup = consumerGroup;
	}

	public Integer getMaxRetry() {
		return maxRetry;
	}

	public void setMaxRetry(Integer maxRetry) {
		this.maxRetry = maxRetry;
	}

	public KafkaConsumerHandlerRetryBackOffProperty getBackoff() {
		return backoff;
	}

	public void setBackoff(
		KafkaConsumerHandlerRetryBackOffProperty backoff) {
		this.backoff = backoff;
	}

	public KafkaConsumerHandlerDlqInfo getDlqInfo() {
		return dlqInfo;
	}

	public void setDlqInfo(
		KafkaConsumerHandlerDlqInfo dlqInfo) {
		this.dlqInfo = dlqInfo;
	}

	@Override
	public String toString() {
		return "KafkaConsumerHandlerProperty{" +
			"mode=" + mode +
			", destinations=" + destinations +
			", consumerGroup=" + consumerGroup +
			", maxRetry=" + maxRetry +
			", backoff=" + backoff +
			", dlqInfo=" + dlqInfo +
			'}';
	}

	public void validate() {
		if (destinations.isEmpty() && consumerGroup.isEmpty()) {
			throw new IllegalArgumentException("At least one topic or consumerGroup must exist.");
		}

		switch (mode) {
			case RETRY -> {
				if (maxRetry == null) {
					throw new IllegalArgumentException("In RETRY mode, 'maxRetry' value must be specified.");
				}
				if (backoff == null) {
					throw new IllegalArgumentException("In RETRY mode, 'backoff' value must be specified.");
				}
			}

			case INFINITY_RETRY -> {
				if (backoff == null) {
					throw new IllegalArgumentException("In INFINITY_RETRY mode, 'backoff' value must be specified.");
				}
			}

			case DLQ -> {
				if (maxRetry == null) {
					throw new IllegalArgumentException("In DLQ mode, 'maxRetry' value must be specified.");
				}
				if (backoff == null) {
					throw new IllegalArgumentException("In DLQ mode, 'backoff' value must be specified.");
				}
				if (dlqInfo == null) {
					throw new IllegalArgumentException("In DLQ mode, 'dlq-info' value must be specified.");
				}
				if (!availableSerializer(dlqInfo)) {
					throw new IllegalArgumentException("Serializer does not exist.");
				}
			}
		}
	}

	private boolean availableSerializer(KafkaConsumerHandlerDlqInfo dlqInfo) {
		try {
			Class.forName(dlqInfo.getKeySerializer());
			Class.forName(dlqInfo.getValueSerializer());
		} catch (ClassNotFoundException e) {
			return false;
		}
		return true;
	}
}
