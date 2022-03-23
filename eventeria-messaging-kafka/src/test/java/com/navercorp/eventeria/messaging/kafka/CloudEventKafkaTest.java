/*
 * Eventeria
 *
 * Copyright (c) 2022-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.eventeria.messaging.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import net.jqwik.api.Example;
import net.jqwik.api.ForAll;
import net.jqwik.api.domains.Domain;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.message.Encoding;
import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import io.cloudevents.kafka.CloudEventDeserializer;
import io.cloudevents.kafka.CloudEventSerializer;
import kafka.server.KafkaConfig;

import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.CloudEventTypeAliasExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.CompositeCloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.MessageCategoryExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.PartitionKeyExtensionsConverter;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.kafka.fixture.EventFixtures;
import com.navercorp.eventeria.messaging.kafka.fixture.TestDomainEvent;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;

class CloudEventKafkaTest {
	private static final EmbeddedKafkaBroker BROKER;

	static {
		BROKER = new EmbeddedKafkaBroker(1, true, 1)
			.kafkaPorts(0)
			.brokerProperty(KafkaConfig.OffsetsTopicReplicationFactorProp(), (short)1)
			.brokerProperty("transaction.state.log.replication.factor", (short)1)
			.brokerProperty("transaction.state.log.min.isr", 1);
		BROKER.afterPropertiesSet();
	}

	private final MessageToCloudEventConverter messageToCloudEventConverter = new DefaultMessageToCloudEventConverter(
		new DefaultCloudEventAttributesConverter(),
		new CompositeCloudEventExtensionsConverter(
			new CloudEventTypeAliasExtensionsConverter(new CloudEventMessageTypeAliasMapper()),
			new MessageCategoryExtensionsConverter(),
			new PartitionKeyExtensionsConverter()
		),
		new JacksonMessageSerializer()
	);
	private final CloudEventToMessageConverter cloudEventToMessageConverter = new DefaultCloudEventToMessageConverter(
		new CloudEventMessageTypeAliasMapper(),
		new JacksonMessageSerializer()
	);

	@Example
	@Domain(EventFixtures.class)
	void configs(@ForAll TestDomainEvent testDomainEvent) throws ExecutionException, InterruptedException {
		// producer
		Properties producerProps = new Properties();
		producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER.getBrokersAsString());
		producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, CloudEventSerializer.class);
		producerProps.put(CloudEventSerializer.ENCODING_CONFIG, Encoding.STRUCTURED);
		producerProps.put(
			CloudEventSerializer.EVENT_FORMAT_CONFIG,
			EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE)
		);

		try (KafkaProducer<String, CloudEvent> producer = new KafkaProducer<>(producerProps)) {
			CloudEvent cloudEvent = this.messageToCloudEventConverter.convert(testDomainEvent);
			producer.send(new ProducerRecord<>("sample.topic", cloudEvent)).get();
		}

		// consumer
		Properties consumerProps = new Properties();
		consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BROKER.getBrokersAsString());
		consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-consumer");
		consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CloudEventDeserializer.class);
		consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

		try (KafkaConsumer<String, CloudEvent> consumer = new KafkaConsumer<>(consumerProps)) {
			consumer.subscribe(Collections.singletonList("sample.topic"));

			ConsumerRecords<String, CloudEvent> records = consumer.poll(Duration.ofSeconds(10));

			records.forEach(rec -> {
				rec.headers().headers("content-type").iterator().forEachRemaining(it ->
					assertThat(new String(it.value(), StandardCharsets.UTF_8)).isEqualTo(JsonFormat.CONTENT_TYPE)
				);

				Message message = cloudEventToMessageConverter.convert(rec.value());
				assertThat(message).isInstanceOf(TestDomainEvent.class);

				TestDomainEvent actual = (TestDomainEvent)message;
				assertThat(actual.getName()).isEqualTo(testDomainEvent.getName());
				assertThat(actual.getId()).isEqualTo(testDomainEvent.getId());
				assertThat(actual.getSourceVersion()).isEqualTo(testDomainEvent.getSourceVersion());
				assertThat(actual.getCorrelationId()).isEqualTo(testDomainEvent.getCorrelationId());
				assertThat(actual.getOperationId()).isEqualTo(testDomainEvent.getOperationId());
				assertThat(actual.getSourceType()).isEqualTo(testDomainEvent.getSourceType());
				assertThat(actual.getSource()).isEqualTo(testDomainEvent.getSource());
				assertThat(actual.getDataSchema()).isEqualTo(testDomainEvent.getDataSchema());
				assertThat(actual.getSubject()).isEqualTo(testDomainEvent.getSubject());
				assertThat(actual.getPartitionKey()).isEqualTo(testDomainEvent.getPartitionKey());
				assertThat(actual.getOccurrenceTime()).isCloseTo(
					testDomainEvent.getOccurrenceTime(),
					new TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
				);
				assertThat(actual.getExtensionNames()).containsAll(testDomainEvent.getExtensionNames());
				testDomainEvent.getExtensionNames().forEach(it ->
					assertThat(actual.getExtension(it)).isEqualTo(testDomainEvent.getExtension(it))
				);
			});
		}
	}
}
