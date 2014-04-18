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
package rickbw.crud.fluent;

import java.util.Objects;
import java.util.concurrent.Callable;

import rickbw.crud.ReadableResource;
import rickbw.crud.pattern.ResourceMerger;
import rx.Observable;
import rx.Observer;
import rx.functions.Func0;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link ReadableResource}s.
 *
 * Note that this class lacks a {@code flatMap} operation, e.g.
 * {@link FluentUpdatableResource#flatMapResponse(Func1)}. This is because
 * the result of executing the flat-mapping function may violate the
 * abstraction of an an idempotent, read-only operation. Consider using
 * {@link ResourceMerger} instead.
 */
public abstract class FluentReadableResource<RSRC> implements ReadableResource<RSRC> {

    public static <RSRC> FluentReadableResource<RSRC> from(final ReadableResource<RSRC> resource) {
        if (resource instanceof FluentReadableResource<?>) {
            return (FluentReadableResource<RSRC>) resource;
        } else {
            return new DelegatingReadableResource<>(resource);
        }
    }

    public <TO> FluentReadableResource<TO> mapValue(final Func1<? super RSRC, ? extends TO> mapper) {
        return new MappingReadableResource<>(this, mapper);
    }

    /**
     * Return a resource that will transparently retry calls to
     * {@link #get()} that throw, as with {@link Observable#retry(int)}.
     * Specifically, any {@link Observable} returned by {@link #get()}
     * will re-subscribe up to {@code maxRetries} times if
     * {@link Observer#onError(Throwable)} is called, rather than propagating
     * that {@code onError} call.
     *
     * If a subscription fails after emitting some number of elements via
     * {@link Observer#onNext(Object)}, those elements will be emitted again
     * on the retry. For example, if an {@code Observable} fails at first
     * after emitting {@code [1, 2]}, then succeeds the second time after
     * emitting {@code [1, 2, 3, 4, 5]}, then the complete sequence of
     * emissions would be {@code [1, 2, 1, 2, 3, 4, 5, onCompleted]}.
     *
     * @param maxRetries    number of retry attempts before failing
     */
    public FluentReadableResource<RSRC> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;
        } else {
            return new RetryingReadableResource<>(this, maxRetries);
        }
    }

    public <TO> FluentReadableResource<TO> lift(final Observable.Operator<TO, RSRC> bind) {
        return new LiftingReadableResource<>(this, bind);
    }

    // TODO: Expose other Observable methods

    /**
     * Return a function that, when called, will call {@link #get()}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func0<Observable<RSRC>> toFunction() {
        return toResourceCallable();
    }

    /**
     * Return a {@link Callable} that delegates to {@link #get()}.
     * The {@code Callable} overrides {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Callable<Observable<RSRC>> toCallable() {
        return toResourceCallable();
    }

    private DelegateObjectMethods.Callable<Observable<RSRC>> toResourceCallable() {
        return new DelegateObjectMethods.Callable<Observable<RSRC>>(this) {
            @Override
            public Observable<RSRC> call() {
                return FluentReadableResource.this.get();
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFluentReadableResource<FROM, TO, T>
    extends FluentReadableResource<TO> {
        protected final FluentResourceStateMixin<ReadableResource<FROM>, T> state;

        protected AbstractFluentReadableResource(
                final ReadableResource<FROM> delegate,
                final T auxiliary) {
            this.state = new FluentResourceStateMixin<>(delegate, auxiliary);
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
            final AbstractFluentReadableResource<?, ?, ?> other = (AbstractFluentReadableResource<?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    /**
     * It may seem that the business of this class could be accomplished by
     * FluentReadableResource itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingReadableResource<RSRC>
    extends AbstractFluentReadableResource<RSRC, RSRC, Void> {
        public DelegatingReadableResource(final ReadableResource<RSRC> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RSRC> get() {
            final Observable<RSRC> rsrc = super.state.getDelegate()
                    .get();
            return rsrc;
        }
    }


    private static final class MappingReadableResource<FROM, TO>
    extends AbstractFluentReadableResource<FROM, TO, Func1<? super FROM, ? extends TO>> {
        public MappingReadableResource(
                final ReadableResource<FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> get() {
            final Observable<TO> rsrc = super.state.getDelegate()
                    .get()
                    .map(super.state.getAuxiliaryState());
            return rsrc;
        }
    }


    private static final class RetryingReadableResource<RSRC>
    extends AbstractFluentReadableResource<RSRC, RSRC, Integer>{
        public RetryingReadableResource(
                final ReadableResource<RSRC> delegate,
                final int maxRetries) {
            super(delegate, maxRetries);
            if (maxRetries <= 0) {
                throw new IllegalArgumentException("maxRetries " + maxRetries + " <= 0");
            }
        }

        @Override
        public Observable<RSRC> get() {
            final Observable<RSRC> rsrc = super.state.getDelegate()
                    .get()
                    .retry(super.state.getAuxiliaryState());
            return rsrc;
        }
    }


    private static final class LiftingReadableResource<FROM, TO>
    extends AbstractFluentReadableResource<FROM, TO, Observable.Operator<TO, FROM>> {
        public LiftingReadableResource(
                final ReadableResource<FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate, bind);
            Objects.requireNonNull(bind, "null operator");
        }

        @Override
        public Observable<TO> get() {
            final Observable<TO> rsrc = super.state.getDelegate()
                    .get()
                    .lift(super.state.getAuxiliaryState());
            return rsrc;
        }
    }

}
