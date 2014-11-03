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

import crud.spi.DeletableSpec;
import rx.Observable;
import rx.Observer;
import rx.functions.Func0;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link DeletableSpec}s.
 */
public abstract class Deletable<RESPONSE> implements DeletableSpec<RESPONSE> {

    /**
     * If the given resource is a {@code Deletable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RESPONSE> Deletable<RESPONSE> from(final DeletableSpec<RESPONSE> resource) {
        if (resource instanceof Deletable<?>) {
            return (Deletable<RESPONSE>) resource;
        } else {
            return new DelegatingDeletableResource<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code Deletable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Deletable<TO> mapResponse(final Func1<? super RESPONSE, ? extends TO> mapper) {
        return new MappingDeletableResource<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform and flatten the
     * responses from this resource. Take care that the given function does
     * not violate the idempotency requirement of
     * {@link DeletableSpec#delete()}.
     *
     * If this method is called on two equal {@code Deletable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Deletable<TO> flatMapResponse(
            final Func1<? super RESPONSE, ? extends Observable<? extends TO>> mapper) {
        return new FlatMappingDeletableResource<>(this, mapper);
    }

    /**
     * Swallow the response(s) on success, emitting only
     * {@link Observer#onCompleted()}. Emit any error to
     * {@link Observer#onError(Throwable)} as usual.
     */
    public <TO> Deletable<TO> flattenResponseToCompletion() {
        final MapToEmptyFunction<RESPONSE, TO> func = MapToEmptyFunction.create();
        return flatMapResponse(func);
    }

    /**
     * Return a resource that will transparently retry calls to
     * {@link #delete()} that throw, as with {@link Observable#retry(long)}.
     * Specifically, any {@link Observable} returned by {@link #delete()}
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
     * If this method is called on two equal {@code Deletable}s,
     * the results will be equal if the max retry counts are equal.
     *
     * @param maxRetries    number of retry attempts before failing
     */
    public Deletable<RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new RetryingDeletableResource<>(this, maxRetries);
        }
    }

    /**
     * Wrap this {@code Deletable} in another one that will
     * pass all observations through a given adapter {@link Observer}, as with
     * {@link Observable#lift(rx.Observable.Operator)}.
     */
    public <TO> Deletable<TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        return new LiftingDeletableResource<>(this, bind);
    }

    // TODO: Expose other Observable methods

    /**
     * Return a function that, when called, will call {@link #delete()}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func0<Observable<RESPONSE>> toFunction() {
        return toResourceCallable();
    }

    /**
     * Return a {@link Callable} that delegates to {@link #delete()}.
     * The {@code Callable} overrides {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Callable<Observable<RESPONSE>> toCallable() {
        return toResourceCallable();
    }

    private DelegateObjectMethods.Callable<Observable<RESPONSE>> toResourceCallable() {
        return new DelegateObjectMethods.Callable<Observable<RESPONSE>>(this) {
            @Override
            public Observable<RESPONSE> call() {
                return Deletable.this.delete();
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFluentDeletableResource<FROM, TO, T>
    extends Deletable<TO> {
        protected final ResourceStateMixin<DeletableSpec<FROM>, T> state;

        protected AbstractFluentDeletableResource(
                final DeletableSpec<FROM> delegate,
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
            final AbstractFluentDeletableResource<?, ?, ?> other = (AbstractFluentDeletableResource<?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    /**
     * It may seem that the business of this class could be accomplished by
     * Deletable itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingDeletableResource<RSRC>
    extends AbstractFluentDeletableResource<RSRC, RSRC, Void> {
        public DelegatingDeletableResource(final DeletableSpec<RSRC> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RSRC> delete() {
            final Observable<RSRC> response = super.state.getDelegate()
                    .delete();
            return response;
        }
    }


    private static final class MappingDeletableResource<FROM, TO>
    extends AbstractFluentDeletableResource<FROM, TO, Func1<? super FROM, ? extends TO>> {
        public MappingDeletableResource(
                final DeletableSpec<FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> delete() {
            final Observable<TO> response = super.state.getDelegate()
                    .delete()
                    .map(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class FlatMappingDeletableResource<FROM, TO>
    extends AbstractFluentDeletableResource<FROM, TO, Func1<? super FROM, ? extends Observable<? extends TO>>> {
        private FlatMappingDeletableResource(
                final DeletableSpec<FROM> delegate,
                final Func1<? super FROM, ? extends Observable<? extends TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> delete() {
            final Observable<TO> response = super.state.getDelegate()
                    .delete()
                    .flatMap(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class RetryingDeletableResource<RESPONSE>
    extends AbstractFluentDeletableResource<RESPONSE, RESPONSE, Integer> {
        public RetryingDeletableResource(
                final DeletableSpec<RESPONSE> delegate,
                final int maxRetries) {
            super(delegate, maxRetries);
            if (maxRetries <= 0) {
                throw new IllegalArgumentException("maxRetries " + maxRetries + " <= 0");
            }
        }

        @Override
        public Observable<RESPONSE> delete() {
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .delete()
                    .retry(super.state.getAuxiliaryState());
            return response;
        }
    }

    private static final class LiftingDeletableResource<FROM, TO>
    extends AbstractFluentDeletableResource<FROM, TO, Observable.Operator<TO, FROM>> {
        public LiftingDeletableResource(
                final DeletableSpec<FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate, bind);
            Objects.requireNonNull(bind, "null operator");
        }

        @Override
        public Observable<TO> delete() {
            final Observable<TO> response = super.state.getDelegate()
                    .delete()
                    .lift(super.state.getAuxiliaryState());
            return response;
        }
    }

}
