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
package crud.transform;

import java.util.Objects;

import crud.core.WritableResource;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of transformations on {@link WritableResource}s.
 */
public abstract class TransformedWritableResource<RSRC, RESPONSE> implements WritableResource<RSRC, RESPONSE> {

    /**
     * If the given resource is a {@code TransformedWritableResource}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC, RESPONSE> TransformedWritableResource<RSRC, RESPONSE> from(
            final WritableResource<RSRC, RESPONSE> resource) {
        if (resource instanceof TransformedWritableResource<?, ?>) {
            return (TransformedWritableResource<RSRC, RESPONSE>) resource;
        } else {
            return new DelegatingWritableResource<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code TransformedWritableResource}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> TransformedWritableResource<RSRC, TO> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<TO>> mapper) {
        return new MappingWritableResource<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform the input resource
     * states before passing them to this resource.
     *
     * If this method is called on two equal {@code TransformedWritableResource}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> TransformedWritableResource<TO, RESPONSE> adaptNewValue(
            final Func1<? super TO, ? extends RSRC> adapter) {
        return new AdaptingWritableResource<>(this, adapter);
    }

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
                return TransformedWritableResource.this.write(newValue);
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractTransformedWritableResource<FROMRS, TORS, FROMRP, TORP, T>
    extends TransformedWritableResource<TORS, TORP> {
        protected final TransformedResourceStateMixin<WritableResource<FROMRS, FROMRP>, T> state;

        protected AbstractTransformedWritableResource(
                final WritableResource<FROMRS, FROMRP> delegate,
                final T auxiliary) {
            this.state = new TransformedResourceStateMixin<>(delegate, auxiliary);
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
            final AbstractTransformedWritableResource<?, ?, ?, ?, ?> other = (AbstractTransformedWritableResource<?, ?, ?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    private static final class DelegatingWritableResource<RSRC, RESPONSE>
    extends AbstractTransformedWritableResource<RSRC, RSRC, RESPONSE, RESPONSE, Void> {
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
    extends AbstractTransformedWritableResource<RSRC, RSRC, FROM, TO, Func1<? super Observable<FROM>, ? extends Observable<TO>>> {
        public MappingWritableResource(
                final WritableResource<RSRC, FROM> delegate,
                final Func1<? super Observable<FROM>, ? extends Observable<TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> write(final RSRC value) {
            final Observable<FROM> response = super.state.getDelegate().write(value);
            final Observable<TO> transformed = super.state.getAuxiliaryState().call(response);
            return transformed;
        }
    }


    private static final class AdaptingWritableResource<FROM, TO, RESPONSE>
    extends AbstractTransformedWritableResource<FROM, TO, RESPONSE, RESPONSE, Func1<? super TO, ? extends FROM>> {
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

}
