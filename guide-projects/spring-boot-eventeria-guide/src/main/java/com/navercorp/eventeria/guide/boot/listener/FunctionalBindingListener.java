package com.navercorp.eventeria.guide.boot.listener;

import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.NotifyToSubscribers;
import com.navercorp.eventeria.guide.boot.domain.PostCreatedEvent;
import com.navercorp.eventeria.guide.boot.publisher.ProgrammaticBindingNotifyCommandPublisher;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessagePublisher;

/**
 * example of functional binding listener
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class FunctionalBindingListener {

	@Qualifier(ProgrammaticBindingNotifyCommandPublisher.OUTBOUND_BEAN_NAME)
	private final SpringMessagePublisher springMessagePublisher;

	/**
	 * Consume {@link PostCreatedEvent} by functional way
	 * and publish {@link NotifyToSubscribers} by programmatic way.
	 */
	@Bean
	Consumer<PostCreatedEvent> toNotifyToSubscribers() {
		return event -> {
			log.info("[CONSUME][Functional][PostCreatedEvent] {}", event);

			springMessagePublisher.publish(
				new NotifyToSubscribers(event.getPostId())
			);
		};
	}

	/**
	 * Consume {@link NotifyToSubscribers} by functional way
	 */
	@Bean
	Consumer<NotifyToSubscribers> consumeNotifyToSubscribersCommand() {
		return command -> log.info("[CONSUME][NotifyToSubscribers] {}", command);
	}
}
