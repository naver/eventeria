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

package com.navercorp.eventeria.messaging.header;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

import io.cloudevents.CloudEvent;
import io.cloudevents.core.format.EventFormat;

import com.navercorp.eventeria.messaging.contract.cloudevents.header.CloudEventHeaderMapper;
import com.navercorp.eventeria.messaging.extension.CloudEventTypeAliasExtension;
import com.navercorp.eventeria.messaging.extension.PartitionKeyExtension;

/**
 * Default {@link CloudEventHeaderMapper} implementation.
 * A result of {@link #toHeaderMap(CloudEvent)} has pairs for kafka
 * <p>
 * CloudEvent attributes are prefixed with `ce_` for use in the message-headers section.
 * @see <a href="https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/bindings/kafka-protocol-binding.md#3231-property-names">cloudevents kafka property names</a>
 */
public class DefaultCloudEventHeaderMapper implements CloudEventHeaderMapper {
	private final EventFormat eventFormat;

	public DefaultCloudEventHeaderMapper(EventFormat eventFormat) {
		this.eventFormat = eventFormat;
	}

	@Override
	public Map<String, Object> toHeaderMap(CloudEvent cloudEvent) {
		Map<String, Object> headers = new HashMap<>();
		headers.put("content-type", this.eventFormat.serializedContentType());
		headers.put("ce_id", cloudEvent.getId());
		OffsetDateTime time = cloudEvent.getTime();
		if (time != null) {
			headers.put("ce_time", cloudEvent.getTime().toString());
		}
		headers.put("ce_type", cloudEvent.getType());

		CloudEventTypeAliasExtension typeAliasExtension = CloudEventTypeAliasExtension.parseExtension(cloudEvent);
		if (typeAliasExtension != null && typeAliasExtension.getTypeAlias() != null) {
			headers.put("ce_" + CloudEventTypeAliasExtension.TYPE_ALIAS_EXTENSION, typeAliasExtension.getTypeAlias());
		}

		PartitionKeyExtension partitionKeyExtension = PartitionKeyExtension.parseExtension(cloudEvent);
		if (partitionKeyExtension != null && partitionKeyExtension.getPartitionKey() != null) {
			headers.put("ce_" + PartitionKeyExtension.PARTITION_KEY_EXTENSION, partitionKeyExtension.getPartitionKey());
		}
		return headers;
	}
}
