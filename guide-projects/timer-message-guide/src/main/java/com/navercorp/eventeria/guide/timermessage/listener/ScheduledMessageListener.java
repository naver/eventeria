package com.navercorp.eventeria.guide.timermessage.listener;

import static java.util.Objects.requireNonNull;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.messaging.SubscribableChannel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.navercorp.eventeria.guide.timermessage.domain.ScheduledMessage;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.InboundChannelBinder;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessageHandler;
import com.navercorp.eventeria.messaging.spring.integration.dsl.MessageSubscriberIntegrationAdapter;
import com.navercorp.eventeria.messaging.spring.integration.router.MessagePayloadTypeRouter;

/**
 * example of programmatic binding 'command' listener
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduledMessageListener {

	/**
	 * bean name for SpringMessageHandler
	 */
	private static final String INBOUND_BEAN_NAME = "inbound-channel-scheduled";

	/**
	 * binding name defined in application.yml
	 */
	private static final String INBOUND_KAFKA_CHANNEL_NAME = "inbound-kafka-channel-scheduled";

	private static final String ROUTER_NAME = "scheduledRouter";

	private final InboundChannelBinder inboundChannelBinder;

	/**
	 * inbound channel for receiving messages from messaging system (kafka)
	 */
	@Bean(INBOUND_KAFKA_CHANNEL_NAME)
	SubscribableChannel inboundChannel() {
		return requireNonNull(
			inboundChannelBinder.getInboundChannel(INBOUND_KAFKA_CHANNEL_NAME)
		);
	}

	/**
	 * intermediate channel which receives transformed messages from messaging system,
	 * and sends then to application
	 */
	@Bean(INBOUND_BEAN_NAME)
	SpringMessageHandler messageHandler() {
		return new SpringMessageHandler();
	}

	/**
	 * configure flow between two channels,
	 * {@link #inboundChannel()} and {@link #messageHandler()}
	 */
	@Bean
	IntegrationFlowAdapter inboundIntegrationFlowAdapter(
		@Qualifier(INBOUND_KAFKA_CHANNEL_NAME) SubscribableChannel inboundChannel,
		CloudEventToMessageConverter cloudEventToMessageConverter,
		@Qualifier(INBOUND_BEAN_NAME) SpringMessageHandler springMessageHandler
	) {
		return new MessageSubscriberIntegrationAdapter(
			inboundChannel,
			cloudEventToMessageConverter,
			springMessageHandler
		);
	}

	/**
	 * configure flow between two channels,
	 * {@link #messageHandler()} and {@link #router()}}
	 */
	@Bean
	IntegrationFlow routerIntegrationFlow(
		@Qualifier(INBOUND_BEAN_NAME) SpringMessageHandler springMessageHandler,
		@Qualifier(ROUTER_NAME) MessagePayloadTypeRouter router
	) {
		return IntegrationFlow.from(springMessageHandler)
			.route(router)
			.get();
	}

	@Bean(ROUTER_NAME)
	MessagePayloadTypeRouter router() {
		return MessagePayloadTypeRouter.register(INBOUND_BEAN_NAME)
			.route(
				ScheduledMessage.class,
				message -> {
					log.info(
						"[ScheduledMessage] scheduled time: {}, current time: {}",
						message.getScheduledAt(),
						Instant.now()
					);
				}
			)
			.done();
	}
}
