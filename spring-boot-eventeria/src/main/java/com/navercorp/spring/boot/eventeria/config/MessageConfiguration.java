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

package com.navercorp.spring.boot.eventeria.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventMessageConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.CloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.converter.MessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.contract.cloudevents.header.CloudEventHeaderMapper;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventMessageReaderWriter;
import com.navercorp.eventeria.messaging.contract.cloudevents.serializer.CloudEventSerializerDeserializer;
import com.navercorp.eventeria.messaging.contract.serializer.MessageDeserializer;
import com.navercorp.eventeria.messaging.contract.serializer.MessageSerializer;
import com.navercorp.eventeria.messaging.contract.serializer.MessageSerializerDeserializer;
import com.navercorp.eventeria.messaging.converter.CloudEventTypeAliasExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.CompositeCloudEventExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventAttributesConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventMessageConverter;
import com.navercorp.eventeria.messaging.converter.DefaultCloudEventToMessageConverter;
import com.navercorp.eventeria.messaging.converter.DefaultMessageToCloudEventConverter;
import com.navercorp.eventeria.messaging.converter.MessageCategoryExtensionsConverter;
import com.navercorp.eventeria.messaging.converter.MessageDeserializeTypeAliasConverter;
import com.navercorp.eventeria.messaging.converter.MessageDeserializeTypeConverter;
import com.navercorp.eventeria.messaging.converter.PartitionKeyExtensionsConverter;
import com.navercorp.eventeria.messaging.jackson.header.JacksonCloudEventHeaderMapper;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonCloudEventReaderWriter;
import com.navercorp.eventeria.messaging.jackson.serializer.JacksonMessageSerializer;
import com.navercorp.eventeria.messaging.spring.converter.SpringCloudEventMessageConverter;
import com.navercorp.eventeria.messaging.typealias.CloudEventMessageTypeAliasMapper;
import com.navercorp.eventeria.messaging.typealias.MessageDeserializeTypeAliasMapper;
import com.navercorp.eventeria.messaging.typealias.MessageSerializeTypeAliasMapper;
import com.navercorp.spring.boot.eventeria.messaging.distribution.MessagePartitionKeyExtractorStrategy;

@Configuration
public class MessageConfiguration {
	@Bean
	@ConditionalOnMissingBean
	public MessageSerializerDeserializer messageSerializerDeserializer() {
		return new JacksonMessageSerializer();
	}

	@Bean
	@ConditionalOnMissingBean
	public CloudEventMessageTypeAliasMapper cloudEventTypeMapper() {
		return new CloudEventMessageTypeAliasMapper();
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageToCloudEventConverter messageToCloudEventConverter(
		MessageSerializeTypeAliasMapper messageSerializeTypeAliasMapper,
		MessageSerializer messageSerializer
	) {
		return new DefaultMessageToCloudEventConverter(
			new DefaultCloudEventAttributesConverter(),
			new CompositeCloudEventExtensionsConverter(
				new CloudEventTypeAliasExtensionsConverter(messageSerializeTypeAliasMapper),
				new MessageCategoryExtensionsConverter(),
				new PartitionKeyExtensionsConverter()
			),
			messageSerializer
		);
	}

	@Bean
	@ConditionalOnMissingBean
	public CloudEventHeaderMapper cloudEventHeaderMapper() {
		return new JacksonCloudEventHeaderMapper();
	}

	@Bean
	@ConditionalOnMissingBean
	public MessageDeserializeTypeConverter messageDeserializeTypeConverter(
		MessageDeserializeTypeAliasMapper messageDeserializeTypeAliasMapper
	) {
		return new MessageDeserializeTypeAliasConverter(messageDeserializeTypeAliasMapper);
	}

	@Bean
	@ConditionalOnMissingBean
	public CloudEventToMessageConverter cloudEventToMessageConverter(
		MessageDeserializeTypeConverter messageDeserializeTypeConverter,
		MessageDeserializer messageDeserializer
	) {
		return new DefaultCloudEventToMessageConverter(
			messageDeserializeTypeConverter,
			messageDeserializer
		);
	}

	@Bean
	@ConditionalOnMissingBean
	public CloudEventMessageConverter cloudEventAndMessageConverter(
		MessageToCloudEventConverter messageToCloudEventConverter,
		CloudEventToMessageConverter cloudEventToMessageConverter
	) {
		return new DefaultCloudEventMessageConverter(
			messageToCloudEventConverter,
			cloudEventToMessageConverter
		);
	}

	@Bean
	@ConditionalOnMissingBean
	public CloudEventMessageReaderWriter cloudEventMessageReaderWriter(
		CloudEventMessageConverter cloudEventMessageConverter
	) {
		return new JacksonCloudEventReaderWriter(cloudEventMessageConverter);
	}

	/**
	 * org.springframework.cloud.stream.config.ContentTypeConfiguration
	 */
	@Bean
	@ConditionalOnMissingBean
	public SpringCloudEventMessageConverter springCloudEventMessageConverter(
		CloudEventSerializerDeserializer cloudEventSerializerDeserializer
	) {
		return new SpringCloudEventMessageConverter(cloudEventSerializerDeserializer);
	}

	/**
	 * org.springframework.cloud.stream.binder.MessageConverterConfigurer$PartitioningInterceptor
	 * org.springframework.cloud.stream.binder.PartitionHandler#getPartitionKeyExtractorStrategy
	 */
	@Bean("messagePartitionKeyExtractorStrategy")
	@ConditionalOnMissingBean
	public MessagePartitionKeyExtractorStrategy messagePartitionKeyExtractorStrategy() {
		return new MessagePartitionKeyExtractorStrategy();
	}
}
