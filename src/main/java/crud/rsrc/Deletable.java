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

import crud.spi.DeletableSpec;
import rx.Observable;
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
            return new DelegatingDeletable<>(resource);
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
    public <TO> Deletable<TO> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<TO>> mapper) {
        return new MappingDeletable<>(this, mapper);
    }

    /**
     * Return a function that, when called, will call {@link #delete()}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func0<Observable<RESPONSE>> toFunction() {
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
    private static abstract class AbstractDeletable<FROM, TO, T>
    extends Deletable<TO> {
        protected final ResourceStateMixin<DeletableSpec<FROM>, T> state;

        protected AbstractDeletable(
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
            final AbstractDeletable<?, ?, ?> other = (AbstractDeletable<?, ?, ?>) obj;
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
    private static final class DelegatingDeletable<RSRC>
    extends AbstractDeletable<RSRC, RSRC, Void> {
        public DelegatingDeletable(final DeletableSpec<RSRC> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RSRC> delete() {
            final Observable<RSRC> response = super.state.getDelegate()
                    .delete();
            return response;
        }
    }


    private static final class MappingDeletable<FROM, TO>
    extends AbstractDeletable<FROM, TO, Func1<? super Observable<FROM>, ? extends Observable<TO>>> {
        public MappingDeletable(
                final DeletableSpec<FROM> delegate,
                final Func1<? super Observable<FROM>, ? extends Observable<TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> delete() {
            final Observable<FROM> response = super.state.getDelegate().delete();
            final Observable<TO> mapped = super.state.getAuxiliaryState().call(response);
            return mapped;
        }
    }

}
