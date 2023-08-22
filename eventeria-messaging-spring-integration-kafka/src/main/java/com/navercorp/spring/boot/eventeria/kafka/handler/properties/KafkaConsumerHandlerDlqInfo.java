package com.navercorp.spring.boot.eventeria.kafka.handler.properties;

public class KafkaConsumerHandlerDlqInfo {
	private String brokers;
	private String destination;
	private Integer partition;
	private String keySerializer = "org.apache.kafka.common.serialization.ByteArraySerializer";
	private String valueSerializer = "org.apache.kafka.common.serialization.ByteArraySerializer";

	public String getBrokers() {
		return brokers;
	}

	public void setBrokers(String brokers) {
		this.brokers = brokers;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public Integer getPartition() {
		return partition;
	}

	public void setPartition(Integer partition) {
		this.partition = partition;
	}

	public String getKeySerializer() {
		return keySerializer;
	}

	public void setKeySerializer(String keySerializer) {
		this.keySerializer = keySerializer;
	}

	public String getValueSerializer() {
		return valueSerializer;
	}

	public void setValueSerializer(String valueSerializer) {
		this.valueSerializer = valueSerializer;
	}

	@Override
	public String toString() {
		return "KafkaConsumerHandlerDlqInfo{" +
			"brokers='" + brokers + '\'' +
			", destination='" + destination + '\'' +
			", partition=" + partition +
			", keySerializer='" + keySerializer + '\'' +
			", valueSerializer='" + valueSerializer + '\'' +
			'}';
	}
}
