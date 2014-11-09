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

import crud.spi.SettableSpec;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link SettableSpec}s.
 */
public abstract class Settable<RSRC, RESPONSE> implements SettableSpec<RSRC, RESPONSE> {

    /**
     * If the given resource is a {@code Settable}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <RSRC, RESPONSE> Settable<RSRC, RESPONSE> from(
            final SettableSpec<RSRC, RESPONSE> resource) {
        if (resource instanceof Settable<?, ?>) {
            return (Settable<RSRC, RESPONSE>) resource;
        } else {
            return new DelegatingSettable<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code Settable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Settable<RSRC, TO> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<TO>> mapper) {
        return new MappingSettable<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform the input resource
     * states before passing them to this resource.
     *
     * If this method is called on two equal {@code Settable}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> Settable<TO, RESPONSE> adaptNewValue(
            final Func1<? super Observable<TO>, ? extends Observable<RSRC>> adapter) {
        return new AdaptingSettable<>(this, adapter);
    }

    /**
     * Return a function that, when called, will call {@link #set(Observable)}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func1<Observable<RSRC>, Observable<RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<Observable<RSRC>, Observable<RESPONSE>>(this) {
            @Override
            public Observable<RESPONSE> call(final Observable<RSRC> newValues) {
                return Settable.this.set(newValues);
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractSettable<FROMRS, TORS, FROMRP, TORP, T>
    extends Settable<TORS, TORP> {
        protected final ResourceStateMixin<SettableSpec<FROMRS, FROMRP>, T> state;

        protected AbstractSettable(
                final SettableSpec<FROMRS, FROMRP> delegate,
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
            final AbstractSettable<?, ?, ?, ?, ?> other = (AbstractSettable<?, ?, ?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    private static final class DelegatingSettable<RSRC, RESPONSE>
    extends AbstractSettable<RSRC, RSRC, RESPONSE, RESPONSE, Void> {
        public DelegatingSettable(final SettableSpec<RSRC, RESPONSE> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RESPONSE> set(final Observable<RSRC> newValue) {
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .set(newValue);
            return response;
        }
    }


    private static final class MappingSettable<RSRC, FROM, TO>
    extends AbstractSettable<RSRC, RSRC, FROM, TO, Func1<? super Observable<FROM>, ? extends Observable<TO>>> {
        public MappingSettable(
                final SettableSpec<RSRC, FROM> delegate,
                final Func1<? super Observable<FROM>, ? extends Observable<TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> set(final Observable<RSRC> newValue) {
            final Observable<FROM> response = super.state.getDelegate().set(newValue);
            final Observable<TO> mapped = super.state.getAuxiliaryState().call(response);
            return mapped;
        }
    }


    private static final class AdaptingSettable<FROM, TO, RESPONSE>
    extends AbstractSettable<FROM, TO, RESPONSE, RESPONSE, Func1<? super Observable<TO>, ? extends Observable<FROM>>> {
        private AdaptingSettable(
                final SettableSpec<FROM, RESPONSE> delegate,
                final Func1<? super Observable<TO>, ? extends Observable<FROM>> adapter) {
            super(delegate, adapter);
            Objects.requireNonNull(adapter, "null function");
        }

        @Override
        public Observable<RESPONSE> set(final Observable<TO> newValue) {
            final Observable<FROM> transformed = super.state.getAuxiliaryState().call(newValue);
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .set(transformed);
            return response;
        }
    }

}
