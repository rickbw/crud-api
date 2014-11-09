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

import crud.spi.UpdatableSpec;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link UpdatableSpec}s.
 */
public abstract class Updatable<UPDATE, RESPONSE> implements UpdatableSpec<UPDATE, RESPONSE> {

    /**
     * If the given resource is a {@code Updatable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <UPDATE, RESPONSE> Updatable<UPDATE, RESPONSE> from(
            final UpdatableSpec<UPDATE, RESPONSE> resource) {
        if (resource instanceof Updatable<?, ?>) {
            return (Updatable<UPDATE, RESPONSE>) resource;
        } else {
            return new DelegatingUpdatable<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code Updatable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Updatable<UPDATE, TO> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<TO>> mapper) {
        return new MappingUpdatable<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform the input updates
     * before passing them to this resource.
     *
     * If this method is called on two equal {@code Updatable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Updatable<TO, RESPONSE> adaptUpdate(
            final Func1<? super Observable<TO>, ? extends Observable<UPDATE>> adapter) {
        return new AdaptingUpdatable<>(this, adapter);
    }

    /**
     * Return a function that, when called, will call {@link #update(Observable)}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func1<Observable<UPDATE>, Observable<RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<Observable<UPDATE>, Observable<RESPONSE>>(this) {
            @Override
            public Observable<RESPONSE> call(final Observable<UPDATE> update) {
                return Updatable.this.update(update);
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractUpdatable<FROMU, TOU, FROMR, TOR, T>
    extends Updatable<TOU, TOR> {
        protected final ResourceStateMixin<UpdatableSpec<FROMU, FROMR>, T> state;

        protected AbstractUpdatable(
                final UpdatableSpec<FROMU, FROMR> delegate,
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
            final AbstractUpdatable<?, ?, ?, ?, ?> other = (AbstractUpdatable<?, ?, ?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    /**
     * It may seem that the business of this class could be accomplished by
     * Updatable itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingUpdatable<UPDATE, RESPONSE>
    extends AbstractUpdatable<UPDATE, UPDATE, RESPONSE, RESPONSE, Void> {
        public DelegatingUpdatable(final UpdatableSpec<UPDATE, RESPONSE> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RESPONSE> update(final Observable<UPDATE> update) {
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .update(update);
            return response;
        }
    }


    private static final class MappingUpdatable<UPDATE, FROM, TO>
    extends AbstractUpdatable<UPDATE, UPDATE, FROM, TO, Func1<? super Observable<FROM>, ? extends Observable<TO>>> {
        private MappingUpdatable(
                final UpdatableSpec<UPDATE, FROM> delegate,
                final Func1<? super Observable<FROM>, ? extends Observable<TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> update(final Observable<UPDATE> update) {
            final Observable<FROM> response = super.state.getDelegate().update(update);
            final Observable<TO> mapped = super.state.getAuxiliaryState().call(response);
            return mapped;
        }
    }


    private static final class AdaptingUpdatable<FROM, TO, RESPONSE>
    extends AbstractUpdatable<FROM, TO, RESPONSE, RESPONSE, Func1<? super Observable<TO>, ? extends Observable<FROM>>> {
        private AdaptingUpdatable(
                final UpdatableSpec<FROM, RESPONSE> delegate,
                final Func1<? super Observable<TO>, ? extends Observable<FROM>> adapter) {
            super(delegate, adapter);
            Objects.requireNonNull(adapter, "null function");
        }

        @Override
        public Observable<RESPONSE> update(final Observable<TO> update) {
            final Observable<FROM> transformed = super.state.getAuxiliaryState().call(update);
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .update(transformed);
            return response;
        }
    }

}
