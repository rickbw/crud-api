/* Copyright 2013–2015 Rick Warren
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

import crud.core.ReadableResource;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;


/**
 * A set of transformations on {@link ReadableResource}s.
 *
 * @author Rick Warren
 */
public abstract class TransformedReadableResource<RSRC> implements ReadableResource<RSRC> {

    /**
     * If the given resource is a {@code TransformedReadableResource}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC> TransformedReadableResource<RSRC> from(final ReadableResource<RSRC> resource) {
        if (resource instanceof TransformedReadableResource<?>) {
            return (TransformedReadableResource<RSRC>) resource;
        } else {
            return new DelegatingReadableResource<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the state of
     * this resource.
     *
     * If this method is called on two equal {@code TransformedReadableResource}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> TransformedReadableResource<TO> mapValue(
            final Func1<? super Observable<RSRC>, ? extends Observable<TO>> mapper) {
        return new MappingReadableResource<>(this, mapper);
    }

    /**
     * Return a function that, when called, will call {@link #read()}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func0<Observable<RSRC>> toFunction() {
        return new Func0<Observable<RSRC>>() {
            @Override
            public Observable<RSRC> call() {
                return TransformedReadableResource.this.read();
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractTransformedReadableResource<FROM, TO, T>
    extends TransformedReadableResource<TO> {
        protected final TransformedResourceStateMixin<ReadableResource<FROM>, T> state;

        protected AbstractTransformedReadableResource(
                final ReadableResource<FROM> delegate,
                final T auxiliary) {
            this.state = new TransformedResourceStateMixin<>(delegate, auxiliary);
        }

        @Override
        public Observable<Void> shutdown() {
            return this.state.getDelegate().shutdown();
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
            final AbstractTransformedReadableResource<?, ?, ?> other = (AbstractTransformedReadableResource<?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    /**
     * It may seem that the business of this class could be accomplished by
     * TransformedReadableResource itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingReadableResource<RSRC>
    extends AbstractTransformedReadableResource<RSRC, RSRC, Void> {
        public DelegatingReadableResource(final ReadableResource<RSRC> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RSRC> read() {
            final Observable<RSRC> rsrc = super.state.getDelegate()
                    .read();
            return rsrc;
        }
    }


    private static final class MappingReadableResource<FROM, TO>
    extends AbstractTransformedReadableResource<FROM, TO, Func1<? super Observable<FROM>, ? extends Observable<TO>>> {
        public MappingReadableResource(
                final ReadableResource<FROM> delegate,
                final Func1<? super Observable<FROM>, ? extends Observable<TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> read() {
            final Observable<FROM> data = super.state.getDelegate().read();
            final Observable<TO> mapped = super.state.getAuxiliaryState().call(data);
            return mapped;
        }
    }

}
