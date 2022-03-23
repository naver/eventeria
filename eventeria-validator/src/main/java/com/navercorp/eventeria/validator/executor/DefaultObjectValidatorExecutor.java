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

package com.navercorp.eventeria.validator.executor;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import javax.validation.ValidationException;
import javax.validation.Validator;

import com.navercorp.eventeria.validator.ObjectValidator;

public class DefaultObjectValidatorExecutor<T> implements ObjectValidatorExecutor<T> {
	private final Validator delegate;

	public DefaultObjectValidatorExecutor() {
		this(Validation.buildDefaultValidatorFactory().getValidator());
	}

	public DefaultObjectValidatorExecutor(Validator delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute(T object) {
		Set<ConstraintViolation<Object>> violations = this.delegate.validate(object);
		if (!violations.isEmpty()) {
			throw new ConstraintViolationException(violations);
		}

		if (object instanceof ObjectValidator) {
			try {
				((ObjectValidator)object).validate();
			} catch (ValidationException e) {
				throw e;
			} catch (RuntimeException e) {
				throw new ValidationException("ObjectValidator ConstraintViolations. type: " + object.getClass(), e);
			}
		}
	}
}
