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

import crud.spi.SettableSpec;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link SettableSpec}s.
 */
public abstract class Settable<RSRC, RESPONSE> implements SettableSpec<RSRC, RESPONSE> {

    /**
     * If the given resource is a {@code Settable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC, RESPONSE> Settable<RSRC, RESPONSE> from(
            final SettableSpec<RSRC, RESPONSE> resource) {
        if (resource instanceof Settable<?, ?>) {
            return (Settable<RSRC, RESPONSE>) resource;
        } else {
            return new DelegatingWritableResource<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code Settable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Settable<RSRC, TO> mapResponse(final Func1<? super RESPONSE, ? extends TO> mapper) {
        return new MappingWritableResource<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform and flatten the
     * responses from this resource. Take care that the given function does
     * not violate the idempotency requirement of
     * {@link SettableSpec#set(Observable)}.
     *
     * If this method is called on two equal {@code Settable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Settable<RSRC, TO> flatMapResponse(
            final Func1<? super RESPONSE, ? extends Observable<? extends TO>> mapper) {
        return new FlatMappingWritableResource<>(this, mapper);
    }

    /**
     * Swallow the response(s) on success, emitting only
     * {@link Observer#onCompleted()}. Emit any error to
     * {@link Observer#onError(Throwable)} as usual.
     */
    public <TO> Settable<RSRC, TO> flattenResponseToCompletion() {
        final MapToEmptyFunction<RESPONSE, TO> func = MapToEmptyFunction.create();
        return flatMapResponse(func);
    }

    /**
     * Create and return a new resource that will transform the input resource
     * states before passing them to this resource.
     *
     * If this method is called on two equal {@code Settable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Settable<TO, RESPONSE> adaptNewValue(
            final Func1<? super TO, ? extends RSRC> adapter) {
        return new AdaptingWritableResource<>(this, adapter);
    }

    /**
     * Return a resource that will transparently retry calls to
     * {@link #set(Observable)} that throw, as with {@link Observable#retry(long)}.
     * Specifically, any {@link Observable} returned by {@link #set(Observable)}
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
     * If this method is called on two equal {@code Settable}s,
     * the results will be equal if the max retry counts are equal.
     *
     * @param maxRetries    number of retry attempts before failing
     */
    public Settable<RSRC, RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new RetryingWritableResource<>(this, maxRetries);
        }
    }

    /**
     * Wrap this {@code Settable} in another one that will
     * pass all observations through a given adapter {@link Observer}, as with
     * {@link Observable#lift(rx.Observable.Operator)}.
     */
    public <TO> Settable<RSRC, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        return new LiftingWritableResource<>(this, bind);
    }

    // TODO: Expose other Observable methods

    /**
     * Return a function that, when called, will call {@link #set(Observable)}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func1<RSRC, Observable<RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<RSRC, Observable<RESPONSE>>(this) {
            @Override
            public Observable<RESPONSE> call(final RSRC newValue) {
                return Settable.this.set(Observable.just(newValue));
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFluentWritableResource<FROMRS, TORS, FROMRP, TORP, T>
    extends Settable<TORS, TORP> {
        protected final ResourceStateMixin<SettableSpec<FROMRS, FROMRP>, T> state;

        protected AbstractFluentWritableResource(
                final SettableSpec<FROMRS, FROMRP> delegate,
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
            final AbstractFluentWritableResource<?, ?, ?, ?, ?> other = (AbstractFluentWritableResource<?, ?, ?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    private static final class DelegatingWritableResource<RSRC, RESPONSE>
    extends AbstractFluentWritableResource<RSRC, RSRC, RESPONSE, RESPONSE, Void> {
        public DelegatingWritableResource(final SettableSpec<RSRC, RESPONSE> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RESPONSE> set(final Observable<? extends RSRC> newValue) {
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .set(newValue);
            return response;
        }
    }


    private static final class MappingWritableResource<RSRC, FROM, TO>
    extends AbstractFluentWritableResource<RSRC, RSRC, FROM, TO, Func1<? super FROM, ? extends TO>> {
        public MappingWritableResource(
                final SettableSpec<RSRC, FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> set(final Observable<? extends RSRC> value) {
            final Observable<TO> response = super.state.getDelegate()
                    .set(value)
                    .map(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class FlatMappingWritableResource<RSRC, FROM, TO>
    extends AbstractFluentWritableResource<RSRC, RSRC, FROM, TO, Func1<? super FROM, ? extends Observable<? extends TO>>> {
        private FlatMappingWritableResource(
                final SettableSpec<RSRC, FROM> delegate,
                final Func1<? super FROM, ? extends Observable<? extends TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> set(final Observable<? extends RSRC> update) {
            final Observable<TO> response = super.state.getDelegate()
                    .set(update)
                    .flatMap(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class AdaptingWritableResource<FROM, TO, RESPONSE>
    extends AbstractFluentWritableResource<FROM, TO, RESPONSE, RESPONSE, Func1<? super TO, ? extends FROM>> {
        private AdaptingWritableResource(
                final SettableSpec<FROM, RESPONSE> delegate,
                final Func1<? super TO, ? extends FROM> adapter) {
            super(delegate, adapter);
            Objects.requireNonNull(adapter, "null function");
        }

        @Override
        public Observable<RESPONSE> set(final Observable<? extends TO> value) {
            final Observable<FROM> transformed = value.map(super.state.getAuxiliaryState());
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .set(transformed);
            return response;
        }
    }


    private static final class RetryingWritableResource<RSRC, RESPONSE>
    extends AbstractFluentWritableResource<RSRC, RSRC, RESPONSE, RESPONSE, Integer> {
        public RetryingWritableResource(
                final SettableSpec<RSRC, RESPONSE> delegate,
                final int maxRetries) {
            super(delegate, maxRetries);
            if (maxRetries <= 0) {
                throw new IllegalArgumentException("maxRetries " + maxRetries + " <= 0");
            }
        }

        @Override
        public Observable<RESPONSE> set(final Observable<? extends RSRC> newResourceState) {
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .set(newResourceState)
                    .retry(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class LiftingWritableResource<RSRC, FROM, TO>
    extends AbstractFluentWritableResource<RSRC, RSRC, FROM, TO, Observable.Operator<TO, FROM>> {
        public LiftingWritableResource(
                final SettableSpec<RSRC, FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate, bind);
            Objects.requireNonNull(bind, "null operator");
        }

        @Override
        public Observable<TO> set(final Observable<? extends RSRC> newResourceState) {
            final Observable<TO> response = super.state.getDelegate()
                    .set(newResourceState)
                    .lift(super.state.getAuxiliaryState());
            return response;
        }
    }

}
