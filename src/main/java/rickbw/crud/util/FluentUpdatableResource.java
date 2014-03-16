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

import rickbw.crud.UpdatableResource;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link UpdatableResource}s.
 */
public abstract class FluentUpdatableResource<UPDATE, RESPONSE> implements UpdatableResource<UPDATE, RESPONSE> {

    protected final UpdatableResource<?, ?> delegate;


    public static <UPDATE, RESPONSE> FluentUpdatableResource<UPDATE, RESPONSE> from(
            final UpdatableResource<? super UPDATE, RESPONSE> resource) {
        if (resource instanceof FluentUpdatableResource<?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentUpdatableResource<UPDATE, RESPONSE> result = (FluentUpdatableResource<UPDATE, RESPONSE>) resource;
            return result;
        } else {
            Preconditions.checkNotNull(resource);
            return new DelegatingUpdatableResource<>(resource);
        }
    }

    public <TO> FluentUpdatableResource<UPDATE, TO> mapResponse(
            final Func1<? super RESPONSE, ? extends TO> mapper) {
        return new MappingUpdatableResource<>(this, mapper);
    }

    public <TO> FluentUpdatableResource<TO, RESPONSE> adaptUpdate(
            final Func1<? super TO, ? extends UPDATE> adapter) {
        return new AdaptingUpdatableResource<>(this, adapter);
    }

    // TODO: Transform Subscriber

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
        final FluentUpdatableResource<?, ?> other = (FluentUpdatableResource<?, ?>) obj;
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

    protected FluentUpdatableResource(final UpdatableResource<?, ?> delegate) {
        this.delegate = Preconditions.checkNotNull(delegate, "null delegate");
    }


    private static final class DelegatingUpdatableResource<UPDATE, RESPONSE>
    extends FluentUpdatableResource<UPDATE, RESPONSE> {
        public DelegatingUpdatableResource(final UpdatableResource<? super UPDATE, RESPONSE> delegate) {
            super(delegate);
        }

        @Override
        public Observable<RESPONSE> update(final UPDATE update) {
            @SuppressWarnings("unchecked")
            final UpdatableResource<? super UPDATE, RESPONSE> rsrc
                    = (UpdatableResource<? super UPDATE, RESPONSE>) super.delegate;
            return rsrc.update(update);
        }
    }


    private static final class MappingUpdatableResource<UPDATE, FROM, TO>
    extends FluentUpdatableResource<UPDATE, TO> {
        private final Func1<? super FROM, ? extends TO> mapper;

        private MappingUpdatableResource(
                final UpdatableResource<UPDATE, FROM> delegate,
                final Func1<? super FROM, ? extends TO> mapper) {
            super(delegate);
            this.mapper = Preconditions.checkNotNull(mapper, "null function");
        }

        @Override
        public Observable<TO> update(final UPDATE update) {
            @SuppressWarnings("unchecked")
            final UpdatableResource<UPDATE, FROM> rsrc = (UpdatableResource<UPDATE, FROM>) super.delegate;
            final Observable<? extends FROM> observable = rsrc.update(update);
            final Observable<TO> mapped = observable.map(this.mapper);
            return mapped;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final MappingUpdatableResource<?, ?, ?> other = (MappingUpdatableResource<?, ?, ?>) obj;
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


    private static final class AdaptingUpdatableResource<FROM, TO, RESPONSE>
    extends FluentUpdatableResource<TO, RESPONSE> {
        private final Func1<? super TO, ? extends FROM> adapter;

        private AdaptingUpdatableResource(
                final UpdatableResource<FROM, RESPONSE> delegate,
                final Func1<? super TO, ? extends FROM> adapter) {
            super(delegate);
            this.adapter = Preconditions.checkNotNull(adapter, "null function");
        }

        @Override
        public Observable<RESPONSE> update(final TO update) {
            final FROM transformed = this.adapter.call(update);
            @SuppressWarnings("unchecked")
            final UpdatableResource<FROM, RESPONSE> rsrc = (UpdatableResource<FROM, RESPONSE>) super.delegate;
            final Observable<RESPONSE> observable = rsrc.update(transformed);
            return observable;
        }

        @Override
        public boolean equals(final Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            final AdaptingUpdatableResource<?, ?, ?> other = (AdaptingUpdatableResource<?, ?, ?>) obj;
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

}
