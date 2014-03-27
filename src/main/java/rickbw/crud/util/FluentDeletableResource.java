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
package rickbw.crud.util;

import rickbw.crud.DeletableResource;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link DeletableResource}s.
 */
public abstract class FluentDeletableResource<RESPONSE> implements DeletableResource<RESPONSE> {

    public static <RESPONSE> FluentDeletableResource<RESPONSE> from(final DeletableResource<RESPONSE> resource) {
        if (resource instanceof FluentDeletableResource<?>) {
            return (FluentDeletableResource<RESPONSE>) resource;
        } else {
            return new DelegatingDeletableResource<>(resource);
        }
    }

    public <TO> FluentDeletableResource<TO> mapResponse(final Func1<? super RESPONSE, ? extends TO> mapper) {
        return new MappingDeletableResource<>(this, mapper);
    }

    /**
     * Return a resource that will transparently retry calls to
     * {@link #delete()} that throw, as with {@link Observable#retry(int)}.
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
     * @param maxRetries    number of retry attempts before failing
     */
    public FluentDeletableResource<RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else {
            return new RetryingDeletableResource<>(this, maxRetries);
        }
    }

    public <TO> FluentDeletableResource<TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        return new LiftingDeletableResource<>(this, bind);
    }

    // TODO: Expose other Observable methods


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFluentDeletableResource<FROM, TO, T>
    extends FluentDeletableResource<TO> {
        protected final FluentResourceStateMixin<DeletableResource<FROM>, T> state;

        protected AbstractFluentDeletableResource(
                final DeletableResource<FROM> delegate,
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
            final AbstractFluentDeletableResource<?, ?, ?> other = (AbstractFluentDeletableResource<?, ?, ?>) obj;
            if (!this.state.equals(other.state)) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + this.state.hashCode();
            return result;
        }
    }


    /**
     * It may seem that the business of this class could be accomplished by
     * FluentDeletableResource itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingDeletableResource<RSRC>
    extends AbstractFluentDeletableResource<RSRC, RSRC, Void> {
        public DelegatingDeletableResource(final DeletableResource<RSRC> delegate) {
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
                final DeletableResource<FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate, mapper);
            Preconditions.checkNotNull(mapper, "null function");
        }

        @Override
        public Observable<TO> delete() {
            final Observable<TO> response = super.state.getDelegate()
                    .delete()
                    .map(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class RetryingDeletableResource<RESPONSE>
    extends AbstractFluentDeletableResource<RESPONSE, RESPONSE, Integer> {
        public RetryingDeletableResource(
                final DeletableResource<RESPONSE> delegate,
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
                final DeletableResource<FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate, bind);
            Preconditions.checkNotNull(bind, "null operator");
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
