package com.navercorp.eventeria.guide.boot.listener;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.messaging.SubscribableChannel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.navercorp.eventeria.guide.boot.domain.AfterPostCommandHandler;
import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.ApplySearchIndex;
import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.RefreshPostRanking;
import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.UpdateUserStatistic;
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
public class ProgrammaticBindingCommandListener {

	/**
	 * bean name for SpringMessageHandler
	 */
	private static final String INBOUND_BEAN_NAME = "inbound-channel-after-post-command";

	/**
	 * binding name defined in application.yml
	 */
	private static final String INBOUND_KAFKA_CHANNEL_NAME = "inbound-kafka-channel-after-post-command";

	private static final String ROUTER_NAME = "afterPostCreationCommandRouter";

	private final InboundChannelBinder inboundChannelBinder;
	private final AfterPostCommandHandler afterPostCommandHandler;

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
	IntegrationFlowAdapter inboundAfterPostCreationCommandIntegrationFlowAdapter(
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
	 * {@link #messageHandler()} and {@link #afterPostCreationCommandRouter()}
	 */
	@Bean
	IntegrationFlow afterPostCreationCommandRouterIntegrationFlow(
		@Qualifier(INBOUND_BEAN_NAME) SpringMessageHandler springMessageHandler,
		@Qualifier(ROUTER_NAME) MessagePayloadTypeRouter postEventRouter
	) {
		return IntegrationFlow.from(springMessageHandler)
			.route(postEventRouter)
			.get();
	}

	@Bean(ROUTER_NAME)
	MessagePayloadTypeRouter afterPostCreationCommandRouter() {
		return MessagePayloadTypeRouter.register(INBOUND_BEAN_NAME)
			.route(
				UpdateUserStatistic.class,
				afterPostCommandHandler::updateUserStatistic
			)
			.route(
				RefreshPostRanking.class,
				afterPostCommandHandler::refreshPostRanking
			)
			.route(
				ApplySearchIndex.class,
				afterPostCommandHandler::applySearchIndex
			)
			.done();
	}
}
