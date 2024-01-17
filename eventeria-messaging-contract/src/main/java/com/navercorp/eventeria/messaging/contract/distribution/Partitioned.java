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

package com.navercorp.eventeria.messaging.contract.distribution;

/**
 * a interface using with {@link com.navercorp.eventeria.messaging.contract.Message} which has a partition key.<br/>
 * for distributed messaging broker like apache-kafka.
 *
 * @see com.navercorp.eventeria.messaging.contract.command.AbstractCommand#getPartitionKey()
 * @see com.navercorp.eventeria.messaging.contract.event.AbstractEvent#getPartitionKey()
 */
public interface Partitioned {
	String getPartitionKey();
}
