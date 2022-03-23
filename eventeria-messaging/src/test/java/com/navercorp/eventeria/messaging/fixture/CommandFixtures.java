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

package com.navercorp.eventeria.messaging.fixture;

import java.time.Instant;
import java.util.Map;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Builders;
import net.jqwik.api.Provide;
import net.jqwik.api.domains.DomainContextBase;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import com.navercorp.eventeria.messaging.arbitrary.ArbitraryUtils;
import com.navercorp.eventeria.messaging.contract.command.AbstractCommand;
import com.navercorp.eventeria.messaging.fixture.CommandFixtures.TestCommand.TestCommandBuilder;

public class CommandFixtures extends DomainContextBase {
	@Provide
	public Arbitrary<TestCommand> testCommand() {
		return Builders.withBuilder(TestCommand::builder)
			.use(ArbitraryUtils.sourceId()).in(TestCommandBuilder::sourceId)
			.use(ArbitraryUtils.sourceVersion()).in(TestCommandBuilder::sourceVersion)
			.use(ArbitraryUtils.currentTime()).in(TestCommandBuilder::occurrenceTime)
			.use(Arbitraries.strings().alpha()).in(TestCommandBuilder::name)
			.use(Arbitraries.maps(
				Arbitraries.strings().alpha().ofMinLength(1),
				Arbitraries.strings().alpha().ofMinLength(1).map(Object.class::cast)
			).ofMinSize(1)).in(TestCommandBuilder::extensions)
			.build(TestCommandBuilder::build);
	}

	@Getter
	@Setter
	@EqualsAndHashCode(callSuper = true)
	@ToString
	public static class TestCommand extends AbstractCommand {
		private String name;

		public TestCommand() {
		}

		public TestCommand(String name) {
			this.name = name;
		}

		@Builder
		public TestCommand(
			String sourceId,
			Long sourceVersion,
			Instant occurrenceTime,
			String name,
			Map<String, Object> extensions
		) {
			super(sourceId, sourceVersion, occurrenceTime);
			this.name = name;
			super.setExtensions(extensions);
		}

		@Override
		public String getSourceType() {
			return "com.navercorp.eventeria.order.Order";
		}
	}
}
