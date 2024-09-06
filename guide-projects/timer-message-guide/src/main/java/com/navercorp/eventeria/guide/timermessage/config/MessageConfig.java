package com.navercorp.eventeria.guide.timermessage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.binding.SubscribableChannelBindingTargetFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import lombok.RequiredArgsConstructor;

import com.navercorp.eventeria.guide.timermessage.domain.ScheduledMessage;
import com.navercorp.eventeria.messaging.contract.serializer.MessageSerializerDeserializer;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.ChannelBindable;
import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.ChannelBinder;
import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.DefaultChannelBinder;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;
import com.navercorp.eventeria.timer.contract.handler.TimerMessageHandler;
import com.navercorp.eventeria.timer.contract.store.TimerMessageStore;
import com.navercorp.eventeria.timer.handler.SimpleTimerMessageHandler;
import com.navercorp.eventeria.timer.spring.integration.handler.SpringTimerMessageHandler;
import com.navercorp.eventeria.timer.store.InMemoryTimerMessageStore;

@Configuration
@RequiredArgsConstructor
public class MessageConfig {

	static {
		EventFormatProvider.getInstance()
			.registerFormat(
				new JsonFormat()
					.withForceExtensionNameLowerCaseDeserialization()
					.withForceIgnoreInvalidExtensionNameDeserialization()
			);
	}

	private final ObjectMapper objectMapper;

	@Bean
	MessageSerializerDeserializer messageSerializerDeserializer() {
		return new JacksonMessageSerializer(objectMapper);
	}

	@Bean
	CloudEventMessageTypeAliasMapper cloudEventMessageTypeAliasMapper() {
		CloudEventMessageTypeAliasMapper typeAliasMapper = new CloudEventMessageTypeAliasMapper();

		typeAliasMapper.addCompatibleTypeAlias(
			ScheduledMessage.class,
			"com.navercorp.eventeria.guide.timermessage.domain.ScheduledMessage"
		);

		return typeAliasMapper;
	}

	@Bean
	ChannelBindable channelBindable() {
		return new ChannelBindable();
	}

	@Bean
	@ConditionalOnMissingBean
	ChannelBinder channelBinder(
		SubscribableChannelBindingTargetFactory bindingTargetFactory,
		ChannelBindable channelBindable
	) {
		return new DefaultChannelBinder(bindingTargetFactory, channelBindable);
	}

	@Bean
	SpringTimerMessageHandler springTimerMessageHandler() {
		TimerMessageStore timerMessageStore = new InMemoryTimerMessageStore();
		TimerMessageHandler timerMessageHandler = new SimpleTimerMessageHandler(timerMessageStore, 1000);

		return new SpringTimerMessageHandler(timerMessageHandler);
	}
}
