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

import rickbw.crud.WritableResource;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link WritableResource}s.
 */
public abstract class FluentWritableResource<RSRC, RESPONSE> implements WritableResource<RSRC, RESPONSE> {

    protected final WritableResource<?, ?> delegate;


    public static <RSRC, RESPONSE> FluentWritableResource<RSRC, RESPONSE> from(
            final WritableResource<? super RSRC, RESPONSE> resource) {
        if (resource instanceof FluentWritableResource<?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentWritableResource<RSRC, RESPONSE> result = (FluentWritableResource<RSRC, RESPONSE>) resource;
            return result;
        } else {
            return new DelegatingWritableResource<>(resource);
        }
    }

    public <TO> FluentWritableResource<RSRC, TO> mapResponse(final Func1<? super RESPONSE, ? extends TO> mapper) {
        return new MappingWritableResource<>(this, mapper);
    }

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
     * @param maxRetries    number of retry attempts before failing
     */
    public FluentWritableResource<RSRC, RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;
        } else {
            return new RetryingWritableResource<>(this, maxRetries);
        }
    }

    public <TO> FluentWritableResource<RSRC, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        return new LiftingWritableResource<>(this, bind);
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
        final FluentWritableResource<?, ?> other = (FluentWritableResource<?, ?>) obj;
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

    protected FluentWritableResource(final WritableResource<?, ?> delegate) {
        this.delegate = Preconditions.checkNotNull(delegate, "null delegate");
    }


    private static final class DelegatingWritableResource<RSRC, RESPONSE>
    extends FluentWritableResource<RSRC, RESPONSE> {
        public DelegatingWritableResource(final WritableResource<? super RSRC, RESPONSE> delegate) {
            super(delegate);
        }

        @Override
        public Observable<RESPONSE> write(final RSRC newValue) {
            @SuppressWarnings("unchecked")
            final WritableResource<? super RSRC, RESPONSE> rsrc = (WritableResource<? super RSRC, RESPONSE>) super.delegate;
            return rsrc.write(newValue);
        }
    }


    private static final class MappingWritableResource<RSRC, FROM, TO>
    extends FluentWritableResource<RSRC, TO> {
        private final Func1<? super FROM, ? extends TO> mapper;

        public MappingWritableResource(
                final WritableResource<RSRC, FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate);
            this.mapper = Preconditions.checkNotNull(mapper, "null function");
        }

        @Override
        public Observable<TO> write(final RSRC value) {
            @SuppressWarnings("unchecked")
            final WritableResource<RSRC, FROM> rsrc = (WritableResource<RSRC, FROM>) super.delegate;
            final Observable<? extends FROM> observable = rsrc.write(value);
            final Observable<TO> mapped = observable.map(this.mapper);
            return mapped;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final MappingWritableResource<?, ?, ?> other = (MappingWritableResource<?, ?, ?>) obj;
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


    private static final class AdaptingWritableResource<FROM, TO, RESPONSE>
    extends FluentWritableResource<TO, RESPONSE> {
        private final Func1<? super TO, ? extends FROM> adapter;

        private AdaptingWritableResource(
                final WritableResource<FROM, RESPONSE> delegate,
                final Func1<? super TO, ? extends FROM> adapter) {
            super(delegate);
            this.adapter = Preconditions.checkNotNull(adapter, "null function");
        }

        @Override
        public Observable<RESPONSE> write(final TO value) {
            final FROM transformed = this.adapter.call(value);
            @SuppressWarnings("unchecked")
            final WritableResource<FROM, RESPONSE> rsrc = (WritableResource<FROM, RESPONSE>) super.delegate;
            final Observable<RESPONSE> observable = rsrc.write(transformed);
            return observable;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final AdaptingWritableResource<?, ?, ?> other = (AdaptingWritableResource<?, ?, ?>) obj;
            return this.adapter.equals(other.adapter);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = super.hashCode();
            result = prime * result + this.adapter.hashCode();
            return result;
        }
    }


    private static final class RetryingWritableResource<RSRC, RESPONSE>
    extends FluentWritableResource<RSRC, RESPONSE> {
        private final int maxRetries;

        public RetryingWritableResource(
                final WritableResource<RSRC, RESPONSE> delegate,
                final int maxRetries) {
            super(delegate);
            if (maxRetries <= 0) {
                throw new IllegalArgumentException("maxRetries " + maxRetries + " <= 0");
            }
            this.maxRetries = maxRetries;
        }

        @Override
        public Observable<RESPONSE> write(final RSRC newResourceState) {
            @SuppressWarnings("unchecked")
            final WritableResource<RSRC, RESPONSE> rsrc = (WritableResource<RSRC, RESPONSE>) super.delegate;
            final Observable<RESPONSE> obs = rsrc.write(newResourceState)
                    .retry(this.maxRetries);
            return obs;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final RetryingWritableResource<?, ?> other = (RetryingWritableResource<?, ?>) obj;
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


    private static final class LiftingWritableResource<RSRC, FROM, TO>
    extends FluentWritableResource<RSRC, TO> {
        private final Observable.Operator<TO, FROM> bind;

        public LiftingWritableResource(
                final WritableResource<RSRC, FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate);
            this.bind = Preconditions.checkNotNull(bind, "null operator");
        }

        @Override
        public Observable<TO> write(final RSRC newResourceState) {
            @SuppressWarnings("unchecked")
            final WritableResource<RSRC, FROM> rsrc = (WritableResource<RSRC, FROM>) super.delegate;
            final Observable<TO> obs = rsrc.write(newResourceState)
                    .lift(this.bind);
            return obs;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final LiftingWritableResource<?, ?, ?> other = (LiftingWritableResource<?, ?, ?>) obj;
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
