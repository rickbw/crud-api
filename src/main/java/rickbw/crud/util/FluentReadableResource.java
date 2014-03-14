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

import rickbw.crud.ReadableResource;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link ReadableResource}s.
 */
public abstract class FluentReadableResource<RSRC> implements ReadableResource<RSRC> {

    /*package*/ final ReadableResource<?> delegate;


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
        final FluentReadableResource<?> other = (FluentReadableResource<?>) obj;
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

    protected FluentReadableResource(final ReadableResource<?> delegate) {
        this.delegate = Preconditions.checkNotNull(delegate, "null delegate");
    }


    private static final class DelegatingReadableResource<RSRC>
    extends FluentReadableResource<RSRC> {
        public DelegatingReadableResource(final ReadableResource<RSRC> delegate) {
            super(delegate);
        }

        @Override
        public Observable<RSRC> get() {
            @SuppressWarnings("unchecked")
            final ReadableResource<RSRC> rsrc = (ReadableResource<RSRC>) super.delegate;
            return rsrc.get();
        }
    }


    private static final class MappingReadableResource<FROM, TO> extends FluentReadableResource<TO> {
        private final Func1<? super FROM, ? extends TO> mapper;

        public MappingReadableResource(
                final ReadableResource<? extends FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate);
            this.mapper = Preconditions.checkNotNull(mapper, "null function");
        }

        @Override
        public Observable<TO> get() {
            @SuppressWarnings("unchecked")
            final ReadableResource<FROM> rsrc = (ReadableResource<FROM>) super.delegate;
            final Observable<FROM> observable = rsrc.get();
            final Observable<TO> mapped = observable.map(this.mapper);
            return mapped;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final MappingReadableResource<?, ?> other = (MappingReadableResource<?, ?>) obj;
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


    private static final class RetryingReadableResource<RSRC> extends FluentReadableResource<RSRC>{
        private final int maxRetries;

        public RetryingReadableResource(
                final ReadableResource<RSRC> delegate,
                final int maxRetries) {
            super(delegate);
            if (maxRetries <= 0) {
                throw new IllegalArgumentException("maxRetries " + maxRetries + " <= 0");
            }
            this.maxRetries = maxRetries;
        }

        @Override
        public Observable<RSRC> get() {
            @SuppressWarnings("unchecked")
            final ReadableResource<RSRC> rsrc = (ReadableResource<RSRC>) super.delegate;
            final Observable<RSRC> obs = rsrc.get()
                    .retry(this.maxRetries);
            return obs;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final RetryingReadableResource<?> other = (RetryingReadableResource<?>) obj;
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


    private static final class LiftingReadableResource<FROM, TO>
    extends FluentReadableResource<TO> {
        private final Observable.Operator<TO, FROM> bind;

        public LiftingReadableResource(
                final ReadableResource<FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate);
            this.bind = Preconditions.checkNotNull(bind, "null operator");
        }

        @Override
        public Observable<TO> get() {
            @SuppressWarnings("unchecked")
            final ReadableResource<FROM> rsrc = (ReadableResource<FROM>) super.delegate;
            final Observable<TO> obs = rsrc.get()
                    .lift(this.bind);
            return obs;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final LiftingReadableResource<?, ?> other = (LiftingReadableResource<?, ?>) obj;
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
