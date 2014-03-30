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

import rickbw.crud.WritableResource;
import rickbw.crud.util.Preconditions;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link WritableResource}s.
 */
public abstract class FluentWritableResource<RSRC, RESPONSE> implements WritableResource<RSRC, RESPONSE> {

    public static <RSRC, RESPONSE> FluentWritableResource<RSRC, RESPONSE> from(
            final WritableResource<RSRC, RESPONSE> resource) {
        if (resource instanceof FluentWritableResource<?, ?>) {
            return (FluentWritableResource<RSRC, RESPONSE>) resource;
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
            Preconditions.checkNotNull(mapper, "null function");
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
            Preconditions.checkNotNull(adapter, "null function");
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
            Preconditions.checkNotNull(bind, "null operator");
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
