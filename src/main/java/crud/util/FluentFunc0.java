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
import java.util.concurrent.Callable;

import com.google.common.base.Throwables;

import rx.functions.Func0;
import rx.functions.Func1;


/**
 * A zero-argument function that supports fluent compositions.
 */
public abstract class FluentFunc0<R> implements Func0<R> {

    /**
     * If the given function is a {@code FluentFunc0}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <R> FluentFunc0<R> from(final Callable<R> func) {
        if (func instanceof FluentFunc0<?>) {
            return (FluentFunc0<R>) func;
        } else {
            return new DelegatingFunction<>(func);
        }
    }

    /**
     * Create and return a new function that will transform the result of
     * this function.
     *
     * If this method is called on two equal {@code FluentFunc0}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <R2> FluentFunc0<R2> mapResult(
            final Func1<? super R, ? extends R2> mapper) {
        return new MappingFunction<>(this, mapper);
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFunction<FROM, TO, T> extends FluentFunc0<TO> {
        protected final FunctionStateMixin<Callable<FROM>, T> state;

        protected AbstractFunction(
                final Callable<FROM> delegate,
                final T auxiliary) {
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
            final AbstractFunction<?, ?, ?> other = (AbstractFunction<?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    /**
     * It may seem that the business of this class could be accomplished by
     * FluentFunc0 itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingFunction<R> extends AbstractFunction<R, R, Void> {
        public DelegatingFunction(final Callable<R> delegate) {
            super(delegate, null);
        }

        @Override
        public R call() {
            try {
                return super.state.getDelegate().call();
            } catch (final Exception ex) {
                throw Throwables.propagate(ex);
            }
        }
    }


    private static final class MappingFunction<FROM, TO>
    extends AbstractFunction<FROM, TO, Func1<? super FROM, ? extends TO>> {
        public MappingFunction(
                final Func0<FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public TO call() {
            try {
                final FROM result = super.state.getDelegate().call();
                final TO mapped = super.state.getAuxiliaryState().call(result);
                return mapped;
            } catch (final Exception ex) {
                throw Throwables.propagate(ex);
            }
        }
    }

}
