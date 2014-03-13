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

    /*package*/ final DeletableResource<?> delegate;


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
        final FluentDeletableResource<?> other = (FluentDeletableResource<?>) obj;
        if (!this.delegate.equals(other.delegate)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        final int result = prime + this.delegate.hashCode();
        return result;
    }

    protected FluentDeletableResource(final DeletableResource<?> delegate) {
        this.delegate = Preconditions.checkNotNull(delegate, "null delegate");
    }


    private static class DelegatingDeletableResource<RESPONSE>
    extends FluentDeletableResource<RESPONSE> {
        public DelegatingDeletableResource(final DeletableResource<RESPONSE> delegate) {
            super(delegate);
        }

        @Override
        public Observable<RESPONSE> delete() {
            @SuppressWarnings("unchecked")
            final DeletableResource<RESPONSE> rsrc = (DeletableResource<RESPONSE>) super.delegate;
            return rsrc.delete();
        }
    }


    private static final class MappingDeletableResource<FROM, TO> extends FluentDeletableResource<TO> {
        private final Func1<? super FROM, ? extends TO> mapper;

        public MappingDeletableResource(
                final DeletableResource<FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate);
            this.mapper = Preconditions.checkNotNull(mapper, "null function");
        }

        @Override
        public Observable<TO> delete() {
            @SuppressWarnings("unchecked")
            final DeletableResource<FROM> rsrc = (DeletableResource<FROM>) super.delegate;
            final Observable<? extends FROM> observable = rsrc.delete();
            final Observable<TO> mapped = observable.map(this.mapper);
            return mapped;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final MappingDeletableResource<?, ?> other = (MappingDeletableResource<?, ?>) obj;
            return this.mapper.equals(other.mapper);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.mapper.hashCode();
            return result;
        }
    }


    private static final class RetryingDeletableResource<RESPONSE>
    extends FluentDeletableResource<RESPONSE> {
        private final int maxRetries;

        public RetryingDeletableResource(
                final DeletableResource<RESPONSE> delegate,
                final int maxRetries) {
            super(delegate);
            if (maxRetries <= 0) {
                throw new IllegalArgumentException("maxRetries " + maxRetries + " <= 0");
            }
            this.maxRetries = maxRetries;
        }

        @Override
        public Observable<RESPONSE> delete() {
            @SuppressWarnings("unchecked")
            final DeletableResource<RESPONSE> rsrc = (DeletableResource<RESPONSE>) super.delegate;
            final Observable<RESPONSE> observable = rsrc.delete()
                    .retry(this.maxRetries);
            return observable;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final RetryingDeletableResource<?> other = (RetryingDeletableResource<?>) obj;
            return this.maxRetries != other.maxRetries;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.maxRetries;
            return result;
        }
    }

    private static final class LiftingDeletableResource<FROM, TO>
    extends FluentDeletableResource<TO> {
        private final Observable.Operator<TO, FROM> bind;

        public LiftingDeletableResource(
                final DeletableResource<FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate);
            this.bind = Preconditions.checkNotNull(bind, "null operator");
        }

        @Override
        public Observable<TO> delete() {
            @SuppressWarnings("unchecked")
            final DeletableResource<FROM> rsrc = (DeletableResource<FROM>) super.delegate;
            final Observable<TO> obs = rsrc.delete()
                    .lift(this.bind);
            return obs;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final LiftingDeletableResource<?, ?> other = (LiftingDeletableResource<?, ?>) obj;
            return this.bind.equals(other.bind);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.bind.hashCode();
            return result;
        }
    }

}
