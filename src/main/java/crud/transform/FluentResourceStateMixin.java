/* Copyright 2013–2014 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package crud.transform;

import java.util.Objects;

import crud.core.Resource;

import rx.functions.Func1;


/**
 * Maintains state on behalf of the {@code Fluent*Resource} classes in this
 * package, in order to minimize repeated boilerplate code.
 */
/*package*/ final class FluentResourceStateMixin<R extends Resource, T> {

    private final R delegate;
    private final T auxiliaryState;


    /**
     * @param delegate  A {@link Resource} that acts on behalf of the
     *                  {@code Resource} that wraps this mix-in. It must not
     *                  be null.
     * @param auxiliary A helper object of another type -- perhaps a
     *                  {@link Func1} -- specific to the concrete type of the
     *                  wrapping {@code Resource}. It may be null.
     */
    public FluentResourceStateMixin(final R delegate, final T auxiliary) {
        this.delegate = Objects.requireNonNull(delegate);
        this.auxiliaryState = auxiliary;    // may be null
    }

    public R getDelegate() {
        return this.delegate;
    }

    public T getAuxiliaryState() {
        return this.auxiliaryState;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FluentResourceStateMixin<?, ?> other = (FluentResourceStateMixin<?, ?>) obj;
        if (!this.delegate.equals(other.delegate)) {
            return false;
        }
        if (this.auxiliaryState == null) {
            if (other.auxiliaryState != null) {
                return false;
            }
        } else if (!this.auxiliaryState.equals(other.auxiliaryState)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.delegate, this.auxiliaryState);
    }

}
