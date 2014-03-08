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
import rx.functions.Func1;


public abstract class FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE>
implements UpdatableResourceProvider<KEY, UPDATE, RESPONSE> {

    public static <KEY, UPDATE, RESPONSE> FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE> from(
            final UpdatableResourceProvider<? super KEY, ? super UPDATE, ? extends RESPONSE> provider) {
        /* XXX: The casts below should be safe, since the API only consumes
         * instances of KEY and UPDATE and only produces instances of RESPONSE.
         * However, the Java generic wildcards don't want to cooperate.
         */
        if (provider instanceof FluentUpdatableResourceProvider<?, ?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE> typedProvider
                    = (FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE>) provider;
            return typedProvider;
        } else {
            return new FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE>() {
                @Override
                public FluentUpdatableResource<UPDATE, RESPONSE> get(final KEY key) {
                    final UpdatableResource<UPDATE, RESPONSE> resource
                            = (UpdatableResource<UPDATE, RESPONSE>) provider.get(key);
                    return FluentUpdatableResource.from(resource);
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
                final UpdatableResource<? super UPDATE, ? extends RESPONSE> resource = outerProvider().get(key);
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
                final UpdatableResource<? super UPDATE, RESPONSE> resource = outerProvider().get(key);
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

    @Override
    public abstract FluentUpdatableResource<UPDATE, RESPONSE> get(KEY key);

    private FluentUpdatableResourceProvider<KEY, UPDATE, RESPONSE> outerProvider() {
        return this;
    }

}
