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
package crud.rsrc;

import java.util.Objects;
import java.util.concurrent.Callable;

import crud.pattern.ResourceBuilder;
import crud.spi.ReadableSpec;
import rx.Observable;
import rx.Observer;
import rx.functions.Func0;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link ReadableSpec}s.
 */
public abstract class Readable<RSRC> implements ReadableSpec<RSRC> {

    /**
     * If the given resource is a {@code Readable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC> Readable<RSRC> from(final ReadableSpec<RSRC> resource) {
        if (resource instanceof Readable<?>) {
            return (Readable<RSRC>) resource;
        } else {
            return new DelegatingReadableResource<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the state of
     * this resource.
     *
     * If this method is called on two equal {@code Readable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Readable<TO> mapValue(final Func1<? super RSRC, ? extends TO> mapper) {
        return new MappingReadableResource<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform and flatten the
     * responses from this resource. Take care that the given function does
     * not violate the requirements of {@link ReadableSpec#get()} calls:
     * it must maintain idempotency, and it must not create observable side
     * effects.
     *
     * If this method is called on two equal {@code Readable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Readable<TO> flatMapValue(
            final Func1<? super RSRC, ? extends Observable<? extends TO>> mapper) {
        return new FlatMappingReadableResource<>(this, mapper);
    }

    /**
     * Return a resource that will transparently retry calls to
     * {@link #get()} that throw, as with {@link Observable#retry(long)}.
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
     * If this method is called on two equal {@code Readable}s,
     * the results will be equal if the max retry counts are equal.
     *
     * @param maxRetries    number of retry attempts before failing
     */
    public Readable<RSRC> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new RetryingReadableResource<>(this, maxRetries);
        }
    }

    /**
     * Wrap this {@code Readable} in another one that will
     * pass all observations through a given adapter {@link Observer}, as with
     * {@link Observable#lift(rx.Observable.Operator)}.
     */
    public <TO> Readable<TO> lift(final Observable.Operator<TO, RSRC> bind) {
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

    /**
     * @throws NullPointerException     If the given class is null.
     * @throws IllegalArgumentException If the given class is not an interface.
     */
    public <R extends ReadableSpec<RSRC>> ResourceBuilder<R> toBuilder(
            final Class<R> rsrcClass) {
        return ResourceBuilder.fromReader(rsrcClass, this);
    }

    private DelegateObjectMethods.Callable<Observable<RSRC>> toResourceCallable() {
        return new DelegateObjectMethods.Callable<Observable<RSRC>>(this) {
            @Override
            public Observable<RSRC> call() {
                return Readable.this.get();
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFluentReadableResource<FROM, TO, T>
    extends Readable<TO> {
        protected final ResourceStateMixin<ReadableSpec<FROM>, T> state;

        protected AbstractFluentReadableResource(
                final ReadableSpec<FROM> delegate,
                final T auxiliary) {
            this.state = new ResourceStateMixin<>(delegate, auxiliary);
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
     * Readable itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingReadableResource<RSRC>
    extends AbstractFluentReadableResource<RSRC, RSRC, Void> {
        public DelegatingReadableResource(final ReadableSpec<RSRC> delegate) {
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
                final ReadableSpec<FROM> delegate,
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


    private static final class FlatMappingReadableResource<FROM, TO>
    extends AbstractFluentReadableResource<FROM, TO, Func1<? super FROM, ? extends Observable<? extends TO>>> {
        private FlatMappingReadableResource(
                final ReadableSpec<FROM> delegate,
                final Func1<? super FROM, ? extends Observable<? extends TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> get() {
            final Observable<TO> response = super.state.getDelegate()
                    .get()
                    .flatMap(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class RetryingReadableResource<RSRC>
    extends AbstractFluentReadableResource<RSRC, RSRC, Integer>{
        public RetryingReadableResource(
                final ReadableSpec<RSRC> delegate,
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
                final ReadableSpec<FROM> delegate,
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