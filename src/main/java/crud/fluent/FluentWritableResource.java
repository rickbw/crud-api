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
package crud.fluent;

import java.util.Objects;

import crud.WritableResource;
import crud.pattern.ResourceMerger;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link WritableResource}s.
 *
 * Note that this class lacks a {@code flatMap} operation, e.g.
 * {@link FluentUpdatableResource#flatMapResponse(Func1)}. This is because
 * the result of executing the flat-mapping function may violate the
 * abstraction of an an idempotent, write-only operation. Consider using
 * {@link ResourceMerger} instead.
 */
public abstract class FluentWritableResource<RSRC, RESPONSE> implements WritableResource<RSRC, RESPONSE> {

    /**
     * If the given resource is a {@code FluentWritableResource}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC, RESPONSE> FluentWritableResource<RSRC, RESPONSE> from(
            final WritableResource<RSRC, RESPONSE> resource) {
        if (resource instanceof FluentWritableResource<?, ?>) {
            return (FluentWritableResource<RSRC, RESPONSE>) resource;
        } else {
            return new DelegatingWritableResource<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code FluentWritableResource}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> FluentWritableResource<RSRC, TO> mapResponse(final Func1<? super RESPONSE, ? extends TO> mapper) {
        return new MappingWritableResource<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform the input resource
     * states before passing them to this resource.
     *
     * If this method is called on two equal {@code FluentWritableResource}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> FluentWritableResource<TO, RESPONSE> adaptNewValue(
            final Func1<? super TO, ? extends RSRC> adapter) {
        return new AdaptingWritableResource<>(this, adapter);
    }

    /**
     * Return a resource that will transparently retry calls to
     * {@link #write(Object)} that throw, as with {@link Observable#retry(int)}.
     * Specifically, any {@link Observable} returned by {@link #write(Object)}
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
     * If this method is called on two equal {@code FluentWritableResource}s,
     * the results will be equal if the max retry counts are equal.
     *
     * @param maxRetries    number of retry attempts before failing
     */
    public FluentWritableResource<RSRC, RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new RetryingWritableResource<>(this, maxRetries);
        }
    }

    /**
     * Wrap this {@code FluentWritableResource} in another one that will
     * pass all observations through a given adapter {@link Observer}, as with
     * {@link Observable#lift(rx.Observable.Operator)}.
     */
    public <TO> FluentWritableResource<RSRC, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        return new LiftingWritableResource<>(this, bind);
    }

    // TODO: Expose other Observable methods

    /**
     * Return a function that, when called, will call {@link #write(Object)}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func1<RSRC, Observable<RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<RSRC, Observable<RESPONSE>>(this) {
            @Override
            public Observable<RESPONSE> call(final RSRC newValue) {
                return FluentWritableResource.this.write(newValue);
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFluentWritableResource<FROMRS, TORS, FROMRP, TORP, T>
    extends FluentWritableResource<TORS, TORP> {
        protected final FluentResourceStateMixin<WritableResource<FROMRS, FROMRP>, T> state;

        protected AbstractFluentWritableResource(
                final WritableResource<FROMRS, FROMRP> delegate,
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
        public DelegatingWritableResource(final WritableResource<RSRC, RESPONSE> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RESPONSE> write(final RSRC newValue) {
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .write(newValue);
            return response;
        }
    }


    private static final class MappingWritableResource<RSRC, FROM, TO>
    extends AbstractFluentWritableResource<RSRC, RSRC, FROM, TO, Func1<? super FROM, ? extends TO>> {
        public MappingWritableResource(
                final WritableResource<RSRC, FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> write(final RSRC value) {
            final Observable<TO> response = super.state.getDelegate()
                    .write(value)
                    .map(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class AdaptingWritableResource<FROM, TO, RESPONSE>
    extends AbstractFluentWritableResource<FROM, TO, RESPONSE, RESPONSE, Func1<? super TO, ? extends FROM>> {
        private AdaptingWritableResource(
                final WritableResource<FROM, RESPONSE> delegate,
                final Func1<? super TO, ? extends FROM> adapter) {
            super(delegate, adapter);
            Objects.requireNonNull(adapter, "null function");
        }

        @Override
        public Observable<RESPONSE> write(final TO value) {
            final FROM transformed = super.state.getAuxiliaryState().call(value);
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .write(transformed);
            return response;
        }
    }


    private static final class RetryingWritableResource<RSRC, RESPONSE>
    extends AbstractFluentWritableResource<RSRC, RSRC, RESPONSE, RESPONSE, Integer> {
        public RetryingWritableResource(
                final WritableResource<RSRC, RESPONSE> delegate,
                final int maxRetries) {
            super(delegate, maxRetries);
            if (maxRetries <= 0) {
                throw new IllegalArgumentException("maxRetries " + maxRetries + " <= 0");
            }
        }

        @Override
        public Observable<RESPONSE> write(final RSRC newResourceState) {
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .write(newResourceState)
                    .retry(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class LiftingWritableResource<RSRC, FROM, TO>
    extends AbstractFluentWritableResource<RSRC, RSRC, FROM, TO, Observable.Operator<TO, FROM>> {
        public LiftingWritableResource(
                final WritableResource<RSRC, FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate, bind);
            Objects.requireNonNull(bind, "null operator");
        }

        @Override
        public Observable<TO> write(final RSRC newResourceState) {
            final Observable<TO> response = super.state.getDelegate()
                    .write(newResourceState)
                    .lift(super.state.getAuxiliaryState());
            return response;
        }
    }

}
