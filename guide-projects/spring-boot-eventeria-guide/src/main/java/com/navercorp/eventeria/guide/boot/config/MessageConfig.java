package com.navercorp.eventeria.guide.boot.config;

import java.util.function.Function;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.stream.binding.SubscribableChannelBindingTargetFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.cloudevents.core.provider.EventFormatProvider;
import io.cloudevents.jackson.JsonFormat;
import lombok.RequiredArgsConstructor;

import com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand.NotifyToSubscribers;
import com.navercorp.eventeria.guide.boot.domain.PostCreatedEvent;
import com.navercorp.eventeria.guide.boot.domain.SerializeOnlyTypeAliasPostCreatedEvent;
import com.navercorp.eventeria.messaging.contract.Message;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventMessageReaderWriter;
import com.navercorp.eventeria.messaging.contract.serializer.MessageSerializerDeserializer;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.ChannelBindable;
import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.ChannelBinder;
import com.navercorp.eventeria.messaging.spring.cloud.stream.binding.DefaultChannelBinder;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;
import com.navercorp.spring.boot.eventeria.support.FunctionalBindingSupports;

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
			PostCreatedEvent.class,
			"com.navercorp.eventeria.guide.boot.domain.PostCreatedEvent"
		);

		typeAliasMapper.addSerializeTypeAlias(
			SerializeOnlyTypeAliasPostCreatedEvent.class,
			"com.navercorp.eventeria.guide.boot.domain.PostCreatedEventSerializeOnlyVersion"
		);

		typeAliasMapper.addCompatibleTypeAlias(
			NotifyToSubscribers.class,
			"com.navercorp.eventeria.guide.boot.domain.AfterPostCreationCommand$NotifyToSubscribers"
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

	/**
	 * Consume and converts messages from messaging system to {@link Message}
	 *
	 * @param cloudEventMessageReaderWriter
	 * @see #cloudEventMessageTypeAliasMapper()
	 * @see spring.cloud.function.definition application.yml
	 * @return type of {@link Message}
	 */
	@Bean
	Function<org.springframework.messaging.Message<byte[]>, Message> transformCloudEventToMessage(
		CloudEventMessageReaderWriter cloudEventMessageReaderWriter
	) {
		return FunctionalBindingSupports.convertToMessage(cloudEventMessageReaderWriter);
	}
}
