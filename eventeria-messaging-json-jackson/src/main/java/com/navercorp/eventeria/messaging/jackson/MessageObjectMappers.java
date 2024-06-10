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

package com.navercorp.eventeria.messaging.jackson;

import static java.util.stream.Collectors.toList;

import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import com.navercorp.eventeria.messaging.jackson.module.MessageModule;

public final class MessageObjectMappers {
	private static final List<Module> REGISTERED_MODULES = ObjectMapper.findModules().stream()
		.filter(module -> !module.getModuleName().equalsIgnoreCase("AfterburnerModule"))
		.collect(toList()); // afterburner only support "public setter" for deserializing

	private static final ObjectMapper MESSAGE_OBJECT_MAPPER = JsonMapper.builder()
		.disable(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS)
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.disable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
		.disable(MapperFeature.ALLOW_FINAL_FIELDS_AS_MUTATORS)
		.disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
		.build()
		.registerModules(REGISTERED_MODULES)
		.registerModule(new MessageModule());

	public static ObjectMapper getMessageObjectMapper() {
		return MESSAGE_OBJECT_MAPPER.copy();
	}
}
