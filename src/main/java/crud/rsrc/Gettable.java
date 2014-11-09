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

import com.google.common.base.Supplier;

import crud.spi.GettableSpec;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link GettableSpec}s.
 */
public abstract class Gettable<RSRC> implements GettableSpec<RSRC> {

    /**
     * If the given resource is a {@code Gettable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC> Gettable<RSRC> from(final GettableSpec<RSRC> resource) {
        if (resource instanceof Gettable<?>) {
            return (Gettable<RSRC>) resource;
        } else {
            return new DelegatingGettable<>(resource);
        }
    }

    public static <RSRC> Gettable<RSRC> from(final Supplier<? extends RSRC> supplier) {
        return new SupplierGettable<>(supplier);
    }

    /**
     * Create and return a new resource that will transform the state of
     * this resource.
     *
     * If this method is called on two equal {@code Gettable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Gettable<TO> mapValue(
            final Func1<? super Observable<RSRC>, ? extends Observable<TO>> mapper) {
        return new MappingGettable<>(this, mapper);
    }

    /**
     * Return a function that, when called, will call {@link #get()}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func0<Observable<RSRC>> toFunction() {
        return new DelegateObjectMethods.Callable<Observable<RSRC>>(this) {
            @Override
            public Observable<RSRC> call() {
                return Gettable.this.get();
            }
        };
    }


    private static final class SupplierGettable<RSRC> extends Gettable<RSRC> {
        private final Supplier<? extends RSRC> supplier;

        public SupplierGettable(final Supplier<? extends RSRC> supplier) {
            this.supplier = Objects.requireNonNull(supplier);
        }

        @Override
        public Observable<RSRC> get() {
            final RSRC value = supplier.get();
            return Observable.just(value);
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + '(' + this.supplier + ')';
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
            final SupplierGettable<?> other = (SupplierGettable<?>) obj;
            return this.supplier.equals(other.supplier);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.supplier);
        }
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractGettable<FROM, TO, T>
    extends Gettable<TO> {
        protected final ResourceStateMixin<GettableSpec<FROM>, T> state;

        protected AbstractGettable(
                final GettableSpec<FROM> delegate,
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
            final AbstractGettable<?, ?, ?> other = (AbstractGettable<?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    /**
     * It may seem that the business of this class could be accomplished by
     * Gettable itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingGettable<RSRC>
    extends AbstractGettable<RSRC, RSRC, Void> {
        public DelegatingGettable(final GettableSpec<RSRC> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RSRC> get() {
            final Observable<RSRC> rsrc = super.state.getDelegate()
                    .get();
            return rsrc;
        }
    }


    private static final class MappingGettable<FROM, TO>
    extends AbstractGettable<FROM, TO, Func1<? super Observable<FROM>, ? extends Observable<TO>>> {
        public MappingGettable(
                final GettableSpec<FROM> delegate,
                final Func1<? super Observable<FROM>, ? extends Observable<TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> get() {
            final Observable<FROM> rsrc = super.state.getDelegate().get();
            final Observable<TO> mapped = super.state.getAuxiliaryState().call(rsrc);
            return mapped;
        }
    }

}
