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

package com.navercorp.eventeria.messaging.contract.meta;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import javax.annotation.Nullable;

import org.junit.jupiter.api.Test;

import com.navercorp.eventeria.messaging.contract.command.AbstractCommand;
import com.navercorp.eventeria.messaging.contract.command.Command;
import com.navercorp.eventeria.messaging.contract.event.AbstractEvent;
import com.navercorp.eventeria.messaging.contract.event.Event;
import com.navercorp.eventeria.messaging.contract.source.EventRaisableSource;

class EventeriaMetaManagerTest {
	@Test
	void getEventTypes() {
		Set<Class<? extends Event>> actual = EventeriaMetaManager.getEventTypes();
		assertThat(actual).isNotEmpty();
		assertThat(actual).containsAnyOf(TestEvent.class);
	}

	@Test
	void getCommandTypes() {
		Set<Class<? extends Command>> actual = EventeriaMetaManager.getCommandTypes();
		assertThat(actual).isNotEmpty();
		assertThat(actual).containsAnyOf(TestCommand.class);
	}

	public static class TestEvent extends AbstractEvent {
		@Nullable
		@Override
		protected Long determineRaisedVersion(EventRaisableSource source) {
			return null;
		}

		@Override
		public String getSourceType() {
			return null;
		}
	}

	public static class TestCommand extends AbstractCommand {
		@Override
		public String getSourceType() {
			return null;
		}
	}
}
