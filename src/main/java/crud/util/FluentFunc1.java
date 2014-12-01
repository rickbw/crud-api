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
package crud.util;

import java.util.Objects;

import rx.functions.Func1;


/**
 * A single-argument function that supports fluent compositions.
 */
public abstract class FluentFunc1<T, R> implements Func1<T, R> {

    /**
     * If the given function is a {@code FluentFunc1}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <T, R> FluentFunc1<T, R> from(final Func1<T, R> resource) {
        if (resource instanceof FluentFunc1<?, ?>) {
            return (FluentFunc1<T, R>) resource;
        } else {
            return new DelegatingFunction<>(resource);
        }
    }

    /**
     * Create and return a new function that will transform the responses from
     * this function.
     *
     * If this method is called on two equal {@code FluentFunc1}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO_R> FluentFunc1<T, TO_R> mapResult(final Func1<? super R, ? extends TO_R> mapper) {
        return new MappingFunction<T, R, TO_R>(this, mapper);
    }

    /**
     * Create and return a new function that will transform the input before
     * passing it to this function.
     *
     * If this method is called on two equal {@code FluentFunc1}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <T2> FluentFunc1<T2, R> adaptInput(final Func1<? super T2, ? extends T> adapter) {
        return new AdaptingFunction<>(this, adapter);
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFunction<FROM_T, TO_T, FROM_R, TO_R, STATE>
    extends FluentFunc1<TO_T, TO_R> {
        protected final FunctionStateMixin<Func1<FROM_T, FROM_R>, STATE> state;

        protected AbstractFunction(
                final Func1<FROM_T, FROM_R> delegate,
                final STATE auxiliary) {
            this.state = new FunctionStateMixin<>(delegate, auxiliary);
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
            final AbstractFunction<?, ?, ?, ?, ?> other = (AbstractFunction<?, ?, ?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    private static final class DelegatingFunction<T, R>
    extends AbstractFunction<T, T, R, R, Void> {
        public DelegatingFunction(final Func1<T, R> delegate) {
            super(delegate, null);
        }

        @Override
        public R call(final T newValue) {
            final R response = super.state.getDelegate().call(newValue);
            return response;
        }
    }


    private static final class MappingFunction<T, FROM_R, TO_R>
    extends AbstractFunction<T, T, FROM_R, TO_R, Func1<? super FROM_R, ? extends TO_R>> {
        public MappingFunction(
                final Func1<T, FROM_R> delegate,
                final Func1<? super FROM_R, ? extends TO_R> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public TO_R call(final T newValue) {
            final FROM_R response = super.state.getDelegate().call(newValue);
            final TO_R mapped = super.state.getAuxiliaryState().call(response);
            return mapped;
        }
    }


    private static final class AdaptingFunction<FROM_T, TO_T, R>
    extends AbstractFunction<FROM_T, TO_T, R, R, Func1<? super TO_T, ? extends FROM_T>> {
        private AdaptingFunction(
                final Func1<FROM_T, R> delegate,
                final Func1<? super TO_T, ? extends FROM_T> adapter) {
            super(delegate, adapter);
            Objects.requireNonNull(adapter, "null function");
        }

        @Override
        public R call(final TO_T newValue) {
            final FROM_T transformed = super.state.getAuxiliaryState().call(newValue);
            final R response = super.state.getDelegate().call(transformed);
            return response;
        }
    }

}
