package com.navercorp.eventeria.guide.timermessage.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.context.WebApplicationContext;

import net.jqwik.api.Arbitraries;

import io.restassured.config.EncoderConfig;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;

import com.navercorp.eventeria.guide.timermessage.config.EmbeddedKafkaHolder;

@SpringBootTest
class ScheduledMessageApiControllerIntegrationTest {

	@BeforeEach
	void setUp(@Autowired WebApplicationContext applicationContext) {
		RestAssuredMockMvc.webAppContextSetup(applicationContext);
		RestAssuredMockMvc.config = RestAssuredMockMvcConfig.config()
			.encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
		RestAssuredMockMvc.resultHandlers(MockMvcResultHandlers.print(System.out));
	}

	@RepeatedTest(value = 3, name = RepeatedTest.LONG_DISPLAY_NAME)
	void schedule() {
		int seconds = Arbitraries.integers().between(2, 10).sample();

		given()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.when()
			.post("?seconds=" + seconds)
			.then()
			.statusCode(200);

		assertPublishedMessage(seconds);
	}

	private void assertPublishedMessage(int seconds) {
		AtomicInteger scheduledMessageCounter = new AtomicInteger();

		new Thread(() -> consumeMessage("scheduled-topic", scheduledMessageCounter)).start();

		// scheduled message wasn't published immediately
		then(scheduledMessageCounter.get()).isEqualTo(0);

		// await publishing schduled message
		var fixedSchedulerSecondBuffer = 1;
		try {
			System.out.println(seconds);
			Thread.sleep((seconds + fixedSchedulerSecondBuffer) * 1000L);
		} catch (InterruptedException e) {
			fail("Test failed");
		}

		// scheduled message published after scheduled seconds
		then(scheduledMessageCounter.get()).isEqualTo(1);
	}

	private void consumeMessage(String topic, AtomicInteger counter) {
		try (Consumer<String, String> consumer = buildConsumer()) {
			List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
			consumer.assign(
				partitionInfos.stream()
					.map(it -> new TopicPartition(it.topic(), it.partition()))
					.toList()
			);

			var record = KafkaTestUtils.getRecords(consumer);
			if (record.isEmpty()) {
				return;
			}

			record.forEach(it -> counter.addAndGet(1));

			consumer.commitSync();
		}
	}

	private Consumer<String, String> buildConsumer() {
		Map<String, Object> properties = KafkaTestUtils.consumerProps(
			"integration-test-group",
			"false",
			EmbeddedKafkaHolder.getEmbeddedKafka()
		);

		// for repeated test
		properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

		return new KafkaConsumer<>(properties);
	}
}
