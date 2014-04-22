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

import java.util.Objects;

import rickbw.crud.UpdatableResource;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link UpdatableResource}s.
 *
 * Note that this class lacks a {@code retry} operation, as in e.g.
 * {@link FluentReadableResource#retry(int)}. This is because updates are not
 * idempotent; hence, retries are not inherently retriable. Applications
 * must handle retry logic, if any, themselves.
 */
public abstract class FluentUpdatableResource<UPDATE, RESPONSE> implements UpdatableResource<UPDATE, RESPONSE> {

    /**
     * If the given resource is a {@code FluentUpdatableResource}, return it.
     * Otherwise, wrap it in a new instance.
     */
    public static <UPDATE, RESPONSE> FluentUpdatableResource<UPDATE, RESPONSE> from(
            final UpdatableResource<UPDATE, RESPONSE> resource) {
        if (resource instanceof FluentUpdatableResource<?, ?>) {
            return (FluentUpdatableResource<UPDATE, RESPONSE>) resource;
        } else {
            return new DelegatingUpdatableResource<>(resource);
        }
    }

    /**
     * Create and return a new resource that will transform the responses from
     * this resource.
     *
     * If this method is called on two equal {@code FluentUpdatableResource}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> FluentUpdatableResource<UPDATE, TO> mapResponse(
            final Func1<? super RESPONSE, ? extends TO> mapper) {
        return new MappingUpdatableResource<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform and flatten the
     * responses from this resource.
     *
     * If this method is called on two equal {@code FluentUpdatableResource}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> FluentUpdatableResource<UPDATE, TO> flatMapResponse(
            final Func1<? super RESPONSE, ? extends Observable<? extends TO>> mapper) {
        return new FlatMappingUpdatableResource<>(this, mapper);
    }

    /**
     * Create and return a new resource that will transform the input updates
     * before passing them to this resource.
     *
     * If this method is called on two equal {@code FluentUpdatableResource}s,
     * the results will be equal if the functions are equal. If equality
     * behavior it important to you (for example, if you intend to keep
     * resources in a {@code HashSet}), consider it in your function
     * implementation.
     */
    public <TO> FluentUpdatableResource<TO, RESPONSE> adaptUpdate(
            final Func1<? super TO, ? extends UPDATE> adapter) {
        return new AdaptingUpdatableResource<>(this, adapter);
    }

    /**
     * Wrap this {@code FluentUpdatableResource} in another one that will
     * pass all observations through a given adapter {@link Observer}, as with
     * {@link Observable#lift(rx.Observable.Operator)}.
     */
    public <TO> FluentUpdatableResource<UPDATE, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        return new LiftingUpdatableResource<>(this, bind);
    }

    // TODO: Expose other Observable methods

    /**
     * Return a function that, when called, will call {@link #update(Object)}.
     * The function object implements {@link Object#equals(Object)},
     * {@link Object#hashCode()}, and {@link Object#toString()} in terms of
     * this resource.
     */
    public Func1<UPDATE, Observable<RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<UPDATE, Observable<RESPONSE>>(this) {
            @Override
            public Observable<RESPONSE> call(final UPDATE update) {
                return FluentUpdatableResource.this.update(update);
            }
        };
    }


    /**
     * Private superclass for the concrete nested classes here. It cannot be
     * combined with its parent class, because it needs additional type
     * parameters that should not be public.
     */
    private static abstract class AbstractFluentUpdatableResource<FROMU, TOU, FROMR, TOR, T>
    extends FluentUpdatableResource<TOU, TOR> {
        protected final FluentResourceStateMixin<UpdatableResource<FROMU, FROMR>, T> state;

        protected AbstractFluentUpdatableResource(
                final UpdatableResource<FROMU, FROMR> delegate,
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
            final AbstractFluentUpdatableResource<?, ?, ?, ?, ?> other = (AbstractFluentUpdatableResource<?, ?, ?, ?, ?>) obj;
            return this.state.equals(other.state);
        }

        @Override
        public int hashCode() {
            return 31 + this.state.hashCode();
        }
    }


    /**
     * It may seem that the business of this class could be accomplished by
     * FluentUpdatableResource itself. However, that would require an
     * additional layer of equals() and hashCode overrides and an unsafe cast.
     */
    private static final class DelegatingUpdatableResource<UPDATE, RESPONSE>
    extends AbstractFluentUpdatableResource<UPDATE, UPDATE, RESPONSE, RESPONSE, Void> {
        public DelegatingUpdatableResource(final UpdatableResource<UPDATE, RESPONSE> delegate) {
            super(delegate, null);
        }

        @Override
        public Observable<RESPONSE> update(final UPDATE update) {
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .update(update);
            return response;
        }
    }


    private static final class MappingUpdatableResource<UPDATE, FROM, TO>
    extends AbstractFluentUpdatableResource<UPDATE, UPDATE, FROM, TO, Func1<? super FROM, ? extends TO>> {
        private MappingUpdatableResource(
                final UpdatableResource<UPDATE, FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> update(final UPDATE update) {
            final Observable<TO> response = super.state.getDelegate()
                    .update(update)
                    .map(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class FlatMappingUpdatableResource<UPDATE, FROM, TO>
    extends AbstractFluentUpdatableResource<UPDATE, UPDATE, FROM, TO, Func1<? super FROM, ? extends Observable<? extends TO>>> {
        private FlatMappingUpdatableResource(
                final UpdatableResource<UPDATE, FROM> delegate,
                final Func1<? super FROM, ? extends Observable<? extends TO>> mapper) {
            super(delegate, mapper);
            Objects.requireNonNull(mapper, "null function");
        }

        @Override
        public Observable<TO> update(final UPDATE update) {
            final Observable<TO> response = super.state.getDelegate()
                    .update(update)
                    .flatMap(super.state.getAuxiliaryState());
            return response;
        }
    }


    private static final class AdaptingUpdatableResource<FROM, TO, RESPONSE>
    extends AbstractFluentUpdatableResource<FROM, TO, RESPONSE, RESPONSE, Func1<? super TO, ? extends FROM>> {
        private AdaptingUpdatableResource(
                final UpdatableResource<FROM, RESPONSE> delegate,
                final Func1<? super TO, ? extends FROM> adapter) {
            super(delegate, adapter);
            Objects.requireNonNull(adapter, "null function");
        }

        @Override
        public Observable<RESPONSE> update(final TO update) {
            final FROM transformed = super.state.getAuxiliaryState().call(update);
            final Observable<RESPONSE> response = super.state.getDelegate()
                    .update(transformed);
            return response;
        }
    }


    private static final class LiftingUpdatableResource<UPDATE, FROM, TO>
    extends AbstractFluentUpdatableResource<UPDATE, UPDATE, FROM, TO, Observable.Operator<TO, FROM>> {
        public LiftingUpdatableResource(
                final UpdatableResource<UPDATE, FROM> delegate,
                final Observable.Operator<TO, FROM> bind) {
            super(delegate, bind);
            Objects.requireNonNull(bind, "null operator");
        }

        @Override
        public Observable<TO> update(final UPDATE update) {
            final Observable<TO> response = super.state.getDelegate()
                    .update(update)
                    .lift(super.state.getAuxiliaryState());
            return response;
        }
    }

}
