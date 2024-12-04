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

package com.navercorp.eventeria.fake.spring.cloud.stream.binder.kafka;

import javax.annotation.Nullable;

import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.transaction.KafkaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.ResourceHolderSupport;
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.SmartTransactionObject;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * transaction manager for replacing {@link KafkaTransactionManager} under fake-binder env.
 *
 * @deprecated use spring-cloud-stream-test-binder instead.
 */
@Deprecated(since = "1.3.0", forRemoval = true)
public class FakeKafkaTransactionManager<K, V> extends KafkaTransactionManager<K, V> {

	public FakeKafkaTransactionManager(ProducerFactory<K, V> producerFactory) {
		super(producerFactory);
	}

	@Override
	protected Object doGetTransaction() {
		FakeKafkaResourceHolder resourceHolder =
			(FakeKafkaResourceHolder)TransactionSynchronizationManager.getResource(getProducerFactory());
		return new FakeKafkaTransactionObject(resourceHolder);
	}

	@Override
	protected boolean isExistingTransaction(Object transaction) {
		FakeKafkaTransactionObject txObject = (FakeKafkaTransactionObject)transaction;
		return txObject.resourceHolder != null;
	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {
		FakeKafkaResourceHolder resourceHolder =
			(FakeKafkaResourceHolder)TransactionSynchronizationManager.getResource(getProducerFactory());
		if (resourceHolder == null) {
			resourceHolder = new FakeKafkaResourceHolder();
			TransactionSynchronizationManager.bindResource(getProducerFactory(), resourceHolder);
			if (TransactionSynchronizationManager.isSynchronizationActive()) {
				TransactionSynchronizationManager
					.registerSynchronization(
						new FakeKafkaResourceHolderSynchronization(resourceHolder, getProducerFactory()));
			}
		}

		FakeKafkaTransactionObject txObject = (FakeKafkaTransactionObject)transaction;
		txObject.resourceHolder = resourceHolder;
	}

	@Override
	protected Object doSuspend(Object transaction) {
		FakeKafkaTransactionObject txObject = (FakeKafkaTransactionObject)transaction;
		txObject.resourceHolder = null;
		return TransactionSynchronizationManager.unbindResource(getProducerFactory());
	}

	@Override
	protected void doResume(@Nullable Object transaction, @Nullable Object suspendedResources) {
		FakeKafkaResourceHolder producerHolder = (FakeKafkaResourceHolder)suspendedResources;
		TransactionSynchronizationManager.bindResource(getProducerFactory(), producerHolder);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		FakeKafkaTransactionObject txObject = (FakeKafkaTransactionObject)status.getTransaction();
		txObject.resourceHolder.commit();
	}

	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		FakeKafkaTransactionObject txObject = (FakeKafkaTransactionObject)status.getTransaction();
		txObject.resourceHolder.rollback();
	}

	@Override
	protected void doSetRollbackOnly(DefaultTransactionStatus status) {
		FakeKafkaTransactionObject txObject = (FakeKafkaTransactionObject)status.getTransaction();
		txObject.resourceHolder.setRollbackOnly();
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {
		FakeKafkaTransactionObject txObject = (FakeKafkaTransactionObject)transaction;
		TransactionSynchronizationManager.unbindResource(getProducerFactory());

		FakeKafkaResourceHolder resourceHolder = txObject.resourceHolder;
		resourceHolder.close();
		resourceHolder.clear();
	}

	private static class FakeKafkaResourceHolder extends ResourceHolderSupport {
		public FakeKafkaResourceHolder() {
			super.setSynchronizedWithTransaction(true);
		}

		public void commit() {
		}

		public void close() {
		}

		public void rollback() {
		}
	}

	private static class FakeKafkaResourceHolderSynchronization
		extends ResourceHolderSynchronization<FakeKafkaResourceHolder, ProducerFactory<?, ?>> {
		private final FakeKafkaResourceHolder resourceHolder;

		public FakeKafkaResourceHolderSynchronization(
			FakeKafkaResourceHolder resourceHolder,
			ProducerFactory<?, ?> resourceKey
		) {
			super(resourceHolder, resourceKey);
			this.resourceHolder = resourceHolder;
		}

		@Override
		public boolean shouldReleaseBeforeCompletion() {
			return false;
		}

		@Override
		public void afterCompletion(int status) {
			try {
				if (status == STATUS_COMMITTED) {
					this.resourceHolder.commit();
				} else {
					this.resourceHolder.rollback();
				}
			} finally {
				super.afterCompletion(status);
			}
		}

		@Override
		public void releaseResource(
			@Nullable FakeKafkaResourceHolder resourceHolder,
			@Nullable ProducerFactory<?, ?> resourceKey
		) {
			if (resourceHolder != null) {
				resourceHolder.close();
			}
		}
	}

	private static class FakeKafkaTransactionObject implements SmartTransactionObject {
		@Nullable
		private FakeKafkaResourceHolder resourceHolder;

		public FakeKafkaTransactionObject(@Nullable FakeKafkaResourceHolder resourceHolder) {
			this.resourceHolder = resourceHolder;
		}

		@Override
		public boolean isRollbackOnly() {
			return this.resourceHolder.isRollbackOnly();
		}

		@Override
		public void flush() {
			// no-op
		}
	}
}
