package com.navercorp.eventeria.guide.timermessage.publisher;

import static java.util.Objects.requireNonNull;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlowAdapter;
import org.springframework.messaging.MessageChannel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.header.CloudEventHeaderMapper;
import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.OutboundChannelBinder;
import com.navercorp.eventeria.messaging.spring.integration.channel.SpringMessagePublisher;
import com.navercorp.eventeria.messaging.spring.integration.dsl.TimerMessagePublisherIntegrationAdapter;
import com.navercorp.eventeria.timer.spring.integration.handler.SpringTimerMessageHandler;

/**
 * example of programmatic binding 'event' publisher
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ScheduledMessagePublisher {

	/**
	 * bean name for SpringMessagePublisher
	 */
	public static final String OUTBOUND_BEAN_NAME = "outbound-channel-scheduled";

	/**
	 * binding name defined in application.yml
	 */
	private static final String OUTBOUND_KAFKA_CHANNEL_NAME = "outbound-kafka-channel-scheduled";

	private final OutboundChannelBinder outboundChannelBinder;

	/**
	 * outbound channel for publishing messages to messaging system (kafka)
	 */
	@Bean(OUTBOUND_KAFKA_CHANNEL_NAME)
	MessageChannel outboundChannel() {
		return requireNonNull(
			outboundChannelBinder.getOutboundChannel(OUTBOUND_KAFKA_CHANNEL_NAME)
		);
	}

	/**
	 * intermediate channel which receives transformed messages from messaging system,
	 * and sends then to application
	 */
	@Bean(OUTBOUND_BEAN_NAME)
	SpringMessagePublisher messagePublisher() {
		return new SpringMessagePublisher();
	}

	/**
	 * configure flow between two channels,
	 * {@link #messagePublisher()} and {@link #outboundChannel()}.
	 */
	@Bean
	IntegrationFlowAdapter outboundIntegrationFlowAdapter(
		@Qualifier(OUTBOUND_KAFKA_CHANNEL_NAME) MessageChannel outboundChannel,
		MessageToCloudEventConverter messageToCloudEventConverter,
		CloudEventHeaderMapper cloudEventHeaderMapper,
		@Qualifier(OUTBOUND_BEAN_NAME) SpringMessagePublisher springMessagePublisher,
		SpringTimerMessageHandler springTimerMessageHandler
	) {
		return new TimerMessagePublisherIntegrationAdapter(
			springMessagePublisher,
			messageToCloudEventConverter,
			cloudEventHeaderMapper,
			outboundChannel,
			springTimerMessageHandler
		);
	}
}
