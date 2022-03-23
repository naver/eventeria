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

package com.navercorp.eventeria.messaging.jackson.module;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;

import com.navercorp.eventeria.messaging.contract.SimpleMessage;
import com.navercorp.eventeria.messaging.contract.command.SimpleCommand;
import com.navercorp.eventeria.messaging.contract.event.SimpleEvent;
import com.navercorp.eventeria.messaging.contract.extension.MessageExtensions;
import com.navercorp.eventeria.messaging.jackson.mixin.MessageExtensionIncludeMixin;
import com.navercorp.eventeria.messaging.jackson.mixin.SimpleMessageMixIn;

public class MessageModule extends SimpleModule {
	public MessageModule() {
		super(MessageModule.class.getName());
		setMixInAnnotation(SimpleMessage.class, SimpleMessageMixIn.class);
		setMixInAnnotation(SimpleEvent.class, SimpleMessageMixIn.class);
		setMixInAnnotation(SimpleCommand.class, SimpleMessageMixIn.class);
		setMixInAnnotation(MessageExtensions.class, MessageExtensionIncludeMixin.class);

		addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
		addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ISO_LOCAL_DATE));
		addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ISO_LOCAL_TIME));
		addSerializer(OffsetDateTime.class, OffsetDateTimeSerializer.INSTANCE);
		addSerializer(ZonedDateTime.class, ZonedDateTimeSerializer.INSTANCE);
		addSerializer(Instant.class, InstantSerializer.INSTANCE);
	}
}
