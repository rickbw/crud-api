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
import rickbw.crud.UpdatableResourceProvider;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link UpdatableResourceProvider}s.
 *
 * Note that this class lacks a {@code retry} operation, as in e.g.
 * {@link FluentReadableResourceProvider#retry(int)}. This is because updates
 * are not idempotent; hence, retries are not inherently retriable.
 * Applications must handle retry logic, if any, themselves.
 */
public abstract class FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE>
implements UpdatableResourceProvider<KEY, UPDATE, RESPONSE> {

    public static <KEY, UPDATE, RESPONSE> FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE> from(
            final UpdatableResourceProvider<KEY, UPDATE, RESPONSE> provider) {
        if (provider instanceof FluentUpdatableResourceProvider<?, ?, ?>) {
            return (FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE>) provider;
        } else {
            return new FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE>() {
                @Override
                public FluentUpdatableResource<UPDATE, RESPONSE> get(final KEY key) {
                    return FluentUpdatableResource.from(provider.get(key));
                }
            };
        }

    }

    public <R> FluentUpdatableResourceProvider<KEY, UPDATE, R> map(
            final Func1<? super RESPONSE, ? extends R> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentUpdatableResourceProvider<KEY, UPDATE, R> result = new FluentUpdatableResourceProvider<KEY, UPDATE, R>() {
            @Override
            public FluentUpdatableResource<UPDATE, R> get(final KEY key) {
                final FluentUpdatableResource<UPDATE, RESPONSE> resource = outerProvider().get(key);
                final UpdatableResource<UPDATE, R> mapped = FluentUpdatableResource.from(resource).mapResponse(mapper);
                return FluentUpdatableResource.from(mapped);
            }
        };
        return result;
    }

    public <U> FluentUpdatableResourceProvider<KEY, U, RESPONSE> adaptUpdate(
            final Func1<? super U, ? extends UPDATE> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentUpdatableResourceProvider<KEY, U, RESPONSE> result = new FluentUpdatableResourceProvider<KEY, U, RESPONSE>() {
            @Override
            public FluentUpdatableResource<U, RESPONSE> get(final KEY key) {
                final UpdatableResource<UPDATE, RESPONSE> resource = outerProvider().get(key);
                final UpdatableResource<U, RESPONSE> transformed = FluentUpdatableResource.from(resource).adaptUpdate(adapter);
                return FluentUpdatableResource.from(transformed);
            }
        };
        return result;
    }

    public <K> FluentUpdatableResourceProvider<K, UPDATE, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentUpdatableResourceProvider<K, UPDATE, RESPONSE> result = new FluentUpdatableResourceProvider<K, UPDATE, RESPONSE>() {
            @Override
            public FluentUpdatableResource<UPDATE, RESPONSE> get(final K key) {
                final KEY transformedKey = adapter.call(key);
                final UpdatableResource<UPDATE, RESPONSE> resource = outerProvider().get(transformedKey);
                return FluentUpdatableResource.from(resource);
            }
        };
        return result;
    }

    public <TO> FluentUpdatableResourceProvider<KEY, UPDATE, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Preconditions.checkNotNull(bind, "null operator");
        return new FluentUpdatableResourceProvider<KEY, UPDATE, TO>() {
            @Override
            public FluentUpdatableResource<UPDATE, TO> get(final KEY key) {
                final FluentUpdatableResource<UPDATE, TO> resource = outerProvider().get(key)
                        .lift(bind);
                return resource;
            }
        };
    }

    @Override
    public abstract FluentUpdatableResource<UPDATE, RESPONSE> get(KEY key);

    private FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE> outerProvider() {
        return this;
    }

}
