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

package com.navercorp.eventeria.messaging.contract;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

public interface Message {
	UUID getId();

	OffsetDateTime getOccurrenceTime();

	@Nullable
	String getSourceId();

	@Nullable
	Long getSourceVersion();

	String getSourceType();

	default URI getSource() {
		String sourceId = this.getSourceId();
		if (sourceId == null) {
			return URI.create("/" + this.getSourceType());
		} else {
			try {
				return URI.create("/" + this.getSourceType() + "/" + URLEncoder.encode(this.getSourceId(), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("Encoding to URL failed. sourceId: " + sourceId + ", encoding: UTF-8");
			}
		}
	}

	default Optional<URI> getDataSchema() {
		return Optional.empty();
	}

	default Optional<String> getSubject() {
		return Optional.empty();
	}

	Optional<UUID> getCorrelationId();

	Optional<String> getOperationId();
}
