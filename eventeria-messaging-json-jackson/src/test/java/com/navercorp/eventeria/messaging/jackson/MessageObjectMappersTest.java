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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import net.jqwik.api.Example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

class MessageObjectMappersTest {
	private final ObjectMapper sut = MessageObjectMappers.getMessageObjectMapper();

	@Example
	void serializeBigDecimal() throws JsonProcessingException {
		// given
		JsonSerializeObject object = JsonSerializeObject.builder()
			.bigDecimal(BigDecimal.valueOf(1000L))
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"bigDecimal\":1000}");
	}

	@Example
	void serializeBigDecimalWithPoint() throws IOException {
		// given
		JsonSerializeObject object = JsonSerializeObject.builder()
			.bigDecimal(BigDecimal.valueOf(1000.0))
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"bigDecimal\":1000.0}");
	}

	@Example
	void deserializeBigDecimal() throws IOException {
		// given
		String json = "{\"bigDecimal\":1000}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getBigDecimal()).isEqualTo(BigDecimal.valueOf(1000L));
	}

	@Example
	void deserializeBigDecimalWithPoint() throws IOException {
		// given
		String json = "{\"bigDecimal\":1000.0}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getBigDecimal()).isEqualTo(BigDecimal.valueOf(1000.0));
	}

	@Example
	void serializeInstant() throws IOException {
		// given
		Instant instant = Instant.ofEpochMilli(1569134566202L);
		JsonSerializeObject object = JsonSerializeObject.builder()
			.instant(instant)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"instant\":\"2019-09-22T06:42:46.202Z\"}");
	}

	@Example
	void serializeInstantExcludeNano() throws IOException {
		// given
		Instant instant = Instant.ofEpochSecond(1569134566, 202405000);
		JsonSerializeObject object = JsonSerializeObject.builder()
			.instant(instant)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"instant\":\"2019-09-22T06:42:46.202405Z\"}");
	}

	@Example
	void deserializeInstant() throws IOException {
		// given
		String json = "{\"instant\":1569134566202}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getInstant()).isEqualTo(Instant.ofEpochMilli(1569134566202L));
	}

	@Example
	void deserializeInstantExcludeNano() throws IOException {
		// given
		String json = "{\"instant\":1569134566.202405000}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getInstant().toEpochMilli()).isEqualTo(1569134566202L);
	}

	@Example
	void deserializeInstantIsoFormat() throws IOException {
		// given
		String json = "{\"instant\":\"2019-09-22T06:42:46.202Z\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getInstant().toEpochMilli()).isEqualTo(1569134566202L);
	}

	@Example
	void serializeZonedDateTime() throws IOException {
		// given
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1569134566202L), ZoneId.of("UTC"));
		JsonSerializeObject object = JsonSerializeObject.builder()
			.zonedDateTime(zonedDateTime)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"zonedDateTime\":\"2019-09-22T06:42:46.202Z\"}");
	}

	@Example
	void serializeZonedDateTimeFormat() throws IOException {
		// given
		ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1569134566202L), ZoneId.of("UTC"));
		JsonSerializeObject object = JsonSerializeObject.builder()
			.zonedDateTimeFormat(zonedDateTime)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"zonedDateTimeFormat\":\"2019-09-22T06:42:46.202+0000\"}");
	}

	@Example
	void deserializeZonedDateTime() throws IOException {
		// given
		String json = "{\"zonedDateTime\":\"2019-09-22T15:42:46.202+0900\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getZonedDateTime().toInstant()).isEqualTo(Instant.ofEpochMilli(1569134566202L));
	}

	@Example
	void deserializeZonedDateTimeWitoutNano() throws IOException {
		// given
		String json = "{\"zonedDateTime\":\"2019-09-22T15:42:46+0900\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getZonedDateTime().toInstant()).isEqualTo(Instant.ofEpochMilli(1569134566000L));
	}

	@Example
	void deserializeZonedDateTimeIsoFormat() throws IOException {
		// given
		String json = "{\"zonedDateTime\":\"2019-09-22T06:42:46.202Z\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getZonedDateTime().toInstant()).isEqualTo(Instant.ofEpochMilli(1569134566202L));
	}

	@Example
	void deserializeZonedDateTimeFormat() throws IOException {
		// given
		String json = "{\"zonedDateTimeFormat\":\"2019-09-22T15:42:46.202+0900\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getZonedDateTimeFormat().toInstant()).isEqualTo(Instant.ofEpochMilli(1569134566202L));
	}

	@Example
	void serializeLocalDateTime() throws IOException {
		// given
		LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(1569134566202L), ZoneId.of("UTC"));
		JsonSerializeObject object = JsonSerializeObject.builder()
			.localDateTime(localDateTime)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"localDateTime\":\"2019-09-22T06:42:46.202\"}");
	}

	@Example
	void deserializeLocalDateTime() throws IOException {
		// given
		String json = "{\"localDateTime\":\"2019-09-22T15:42:46.202\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getLocalDateTime()
			.atZone(ZoneId.of("UTC"))
			.toInstant()
			.toEpochMilli())
			.isEqualTo(1569166966202L);
	}

	@Example
	void deserializeLocalDateTimeExcludeNano() throws IOException {
		// given
		String json = "{\"localDateTime\":\"2019-09-22T15:42:46\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getLocalDateTime()
			.atZone(ZoneId.of("UTC"))
			.toInstant()
			.toEpochMilli())
			.isEqualTo(1569166966000L);
	}

	@Example
	void serializeLocalDate() throws IOException {
		// given
		LocalDate localDate = LocalDate.parse("2019-09-22");
		JsonSerializeObject object = JsonSerializeObject.builder()
			.localDate(localDate)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"localDate\":\"2019-09-22\"}");
	}

	@Example
	void serializeLocalDateFormat() throws IOException {
		// given
		LocalDate localDate = LocalDate.parse("2019-09-22");
		JsonSerializeObject object = JsonSerializeObject.builder()
			.localDateFormat(localDate)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"localDateFormat\":\"20190922\"}");
	}

	@Example
	void deserializeLocalDate() throws IOException {
		// given
		String json = "{\"localDate\":\"2019-09-22\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getLocalDate()).isEqualTo(LocalDate.parse("2019-09-22"));
	}

	@Example
	void deserializeLocalDateFormat() throws IOException {
		// given
		String json = "{\"localDateFormat\":\"20190922\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getLocalDateFormat()).isEqualTo(LocalDate.parse("2019-09-22"));
	}

	@Example
	void serializeLocalTime() throws IOException {
		// given
		LocalTime localTime = LocalTime.parse("15:42");
		JsonSerializeObject object = JsonSerializeObject.builder()
			.localTime(localTime)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"localTime\":\"15:42:00\"}");
	}

	@Example
	void serializeLocalTimeFormat() throws IOException {
		// given
		LocalTime localTime = LocalTime.parse("15:42");
		JsonSerializeObject object = JsonSerializeObject.builder()
			.localTimeFormat(localTime)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"localTimeFormat\":\"1542\"}");
	}

	@Example
	void serializeLocalTimeFullFormat() throws IOException {
		// given
		LocalTime localTime = LocalTime.parse("15:42");
		JsonSerializeObject object = JsonSerializeObject.builder()
			.localTimeFullFormat(localTime)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"localTimeFullFormat\":\"154200\"}");
	}

	@Example
	void deserializeLocalTime() throws IOException {
		// given
		String json = "{\"localTime\":\"15:42\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getLocalTime()).isEqualTo(LocalTime.parse("15:42"));
	}

	@Example
	void deserializeLocalTimeWithSecond() throws IOException {
		// given
		String json = "{\"localTime\":\"15:42:00\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getLocalTime()).isEqualTo(LocalTime.parse("15:42"));
	}

	@Example
	void deserializeLocalTimeFormat() throws IOException {
		// given
		String json = "{\"localTimeFormat\":\"1542\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getLocalTimeFormat()).isEqualTo(LocalTime.parse("15:42"));
	}

	@Example
	void deserializeLocalTimeFullFormat() throws IOException {
		// given
		String json = "{\"localTimeFullFormat\":\"154200\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getLocalTimeFullFormat()).isEqualTo(LocalTime.parse("15:42"));
	}

	@Example
	void serializeDuration() throws IOException {
		// given
		Duration duration = Duration.ofDays(3L);
		JsonSerializeObject object = JsonSerializeObject.builder()
			.duration(duration)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"duration\":259200000}");
	}

	@Example
	void deserializeDuration() throws IOException {
		// given
		String json = "{\"duration\":259200000}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getDuration()).isEqualTo(Duration.ofDays(3L));
	}

	@Example
	void deserializeDurationFormat() throws IOException {
		// given
		String json = "{\"duration\":\"PT72H\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getDuration()).isEqualTo(Duration.ofDays(3L));
	}

	@Example
	void serializeOffsetDateTime() throws IOException {
		// given
		OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(1569134566202L),
			ZoneId.of("UTC"));
		JsonSerializeObject object = JsonSerializeObject.builder()
			.offsetDateTime(offsetDateTime)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"offsetDateTime\":\"2019-09-22T06:42:46.202Z\"}");
	}

	@Example
	void deserializeOffsetDateTime() throws IOException {
		// given
		String json = "{\"offsetDateTime\":\"2019-09-22T06:42:46.202Z\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getOffsetDateTime().toInstant()).isEqualTo(Instant.ofEpochMilli(1569134566202L));
	}

	@Example
	void deserializeOffsetDateTimeTimestamp() throws IOException {
		// given
		String json = "{\"offsetDateTime\":1569134566202}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getOffsetDateTime().toInstant()).isEqualTo(Instant.ofEpochMilli(1569134566202L));
	}

	@Example
	void deserializeOffsetDateTimeIsoFormat() throws IOException {
		// given
		String json = "{\"offsetDateTime\":\"2019-09-22T06:42:46.202Z\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getOffsetDateTime().toInstant()).isEqualTo(Instant.ofEpochMilli(1569134566202L));
	}

	@Example
	void serializeYear() throws IOException {
		// given
		Year year = Year.of(2019);
		JsonSerializeObject object = JsonSerializeObject.builder()
			.year(year)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"year\":\"2019\"}");
	}

	@Example
	void deserializeYear() throws IOException {
		// given
		String json = "{\"year\":2019}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getYear()).isEqualTo(Year.of(2019));
	}

	@Example
	void deserializeYearString() throws IOException {
		// given
		String json = "{\"year\":\"2019\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getYear()).isEqualTo(Year.of(2019));
	}

	@Example
	void serializeMonthDay() throws IOException {
		// given
		MonthDay monthDay = MonthDay.of(9, 22);
		JsonSerializeObject object = JsonSerializeObject.builder()
			.monthDay(monthDay)
			.build();

		// when
		String actual = this.sut.writeValueAsString(object);

		// then
		assertThat(actual).isEqualTo("{\"monthDay\":\"--09-22\"}");
	}

	@Example
	void deserializeMonthDay() throws IOException {
		// given
		String json = "{\"monthDay\":\"--09-22\"}";

		// when
		JsonSerializeObject actual = this.sut.readValue(json, JsonSerializeObject.class);

		// then
		assertThat(actual.getMonthDay()).isEqualTo(MonthDay.of(9, 22));
	}

	@Builder
	@Getter
	@Setter(AccessLevel.PACKAGE)
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonInclude(JsonInclude.Include.NON_NULL)
	static class JsonSerializeObject {
		private BigDecimal bigDecimal;
		private Instant instant;
		private ZonedDateTime zonedDateTime;
		@JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
		private ZonedDateTime zonedDateTimeFormat;
		private LocalDateTime localDateTime;
		private LocalDate localDate;
		@JsonFormat(pattern = "yyyyMMdd")
		private LocalDate localDateFormat;
		private LocalTime localTime;
		@JsonFormat(pattern = "HHmm")
		private LocalTime localTimeFormat;
		@JsonFormat(pattern = "HHmmss")
		private LocalTime localTimeFullFormat;
		private Duration duration;
		private OffsetDateTime offsetDateTime;
		private Year year;
		private MonthDay monthDay;
	}
}
