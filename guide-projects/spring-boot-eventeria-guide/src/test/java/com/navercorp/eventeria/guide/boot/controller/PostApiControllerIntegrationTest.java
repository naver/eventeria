package com.navercorp.eventeria.guide.boot.controller;

import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import com.navercorp.eventeria.guide.boot.config.EmbeddedKafkaHolder;
import com.navercorp.eventeria.guide.boot.controller.PostApiController.CreatePostRequest;

@SpringBootTest
class PostApiControllerIntegrationTest {

	@BeforeEach
	void setUp(@Autowired WebApplicationContext applicationContext) {
		RestAssuredMockMvc.webAppContextSetup(applicationContext);
		RestAssuredMockMvc.config = RestAssuredMockMvcConfig.config()
			.encoderConfig(EncoderConfig.encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false));
		RestAssuredMockMvc.resultHandlers(MockMvcResultHandlers.print(System.out));
	}

	@Test
	void registerPost() {
		long postId = Arbitraries.longs().greaterOrEqual(1).sample();

		given()
			.contentType(MediaType.APPLICATION_JSON_VALUE)
			.body(
				new CreatePostRequest(
					postId,
					"testWriterId",
					"Hello, World!"
				)
			)
			.when()
			.post("/posts/" + postId)
			.then()
			.statusCode(200);

		assertPublishedMessageCount();
	}

	private void assertPublishedMessageCount() {
		int expectedPostEventCount = 1 + 1; // PostCreatedEvent + SerializeOnlyTypeAliasPostCreatedEvent
		int expectedAfterPostCommandCount = 3;
		int expectedNotifyCommandCount = 1;

		AtomicInteger actualPostEventCounter = new AtomicInteger();
		AtomicInteger actualAfterPostCommandCounter = new AtomicInteger();
		AtomicInteger actualNotifyCommandCounter = new AtomicInteger();

		var latch = new CountDownLatch(
			expectedPostEventCount + expectedAfterPostCommandCount + expectedNotifyCommandCount
		);

		new Thread(() -> consumeMessage("post-event", latch, actualPostEventCounter)).start();
		new Thread(() -> consumeMessage("after-post-command", latch, actualAfterPostCommandCounter)).start();
		new Thread(() -> consumeMessage("notify-command", latch, actualNotifyCommandCounter)).start();

		try {
			if (!latch.await(10L, TimeUnit.SECONDS)) {
				fail("Test failed due to a time out.");
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		// PostCreatedEvent + SerializeOnlyTypeAliasPostCreatedEvent published
		then(actualPostEventCounter.get()).isEqualTo(expectedPostEventCount);

		// continuous command published for PostCreatedEvent only.
		// Because failed to deserialize SerializeOnlyTypeAliasPostCreatedEvent
		then(actualAfterPostCommandCounter.get()).isEqualTo(expectedAfterPostCommandCount);
		then(actualNotifyCommandCounter.get()).isEqualTo(expectedNotifyCommandCount);
	}

	private void consumeMessage(String topic, CountDownLatch latch, AtomicInteger counter) {
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

			record.forEach(it -> {
				latch.countDown();
				counter.addAndGet(1);
			});

			consumer.commitSync();
		}
	}

	private Consumer<String, String> buildConsumer() {
		return new KafkaConsumer<>(
			KafkaTestUtils.consumerProps(
				"integration-test-group",
				"false",
				EmbeddedKafkaHolder.getEmbeddedKafka()
			)
		);
	}
}
