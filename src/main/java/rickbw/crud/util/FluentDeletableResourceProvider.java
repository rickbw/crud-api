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

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rx.util.functions.Func1;


public abstract class FluentDeletableResourceProvider<KEY, RESPONSE>
implements DeletableResourceProvider<KEY, RESPONSE> {

    public static <KEY, RESPONSE> FluentDeletableResourceProvider<KEY, RESPONSE> from(
            final DeletableResourceProvider<? super KEY, ? extends RESPONSE> provider) {
        /* XXX: The casts below should be safe, since the API only consumes
         * instances of KEY and only produces instances of RESPONSE.
         * However, the Java generic wildcards don't want to cooperate.
         */
        if (provider instanceof FluentDeletableResourceProvider<?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentDeletableResourceProvider<KEY, RESPONSE> typedProvider
                    = (FluentDeletableResourceProvider<KEY, RESPONSE>) provider;
            return typedProvider;
        } else {
            return new FluentDeletableResourceProvider<KEY, RESPONSE>() {
                @Override
                public DeletableResource<RESPONSE> get(final KEY key) {
                    final DeletableResource<RESPONSE> resource
                            = (DeletableResource<RESPONSE>) provider.get(key);
                    return resource;
                }
            };
        }
    }

    public <R> FluentDeletableResourceProvider<KEY, R> map(
            final Func1<? super RESPONSE, ? extends R> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentDeletableResourceProvider<KEY, R> result = new FluentDeletableResourceProvider<KEY, R>() {
            @Override
            public DeletableResource<R> get(final KEY key) {
                final DeletableResource<? extends RESPONSE> resource = outerProvider().get(key);
                final DeletableResource<R> mapped = FluentDeletableResource.from(resource).mapResponse(mapper);
                return mapped;
            }
        };
        return result;
    }

    public <K> FluentDeletableResourceProvider<K, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentDeletableResourceProvider<K, RESPONSE> result = new FluentDeletableResourceProvider<K, RESPONSE>() {
            @Override
            public DeletableResource<RESPONSE> get(final K key) {
                final KEY transformedKey = adapter.call(key);
                final DeletableResource<RESPONSE> resource = outerProvider().get(transformedKey);
                return resource;
            }
        };
        return result;
    }

    private FluentDeletableResourceProvider<KEY, RESPONSE> outerProvider() {
        return this;
    }

}
