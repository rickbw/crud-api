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

import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rx.functions.Func1;


public abstract class FluentReadableResourceProvider<KEY, RSRC>
implements ReadableResourceProvider<KEY, RSRC> {

    public static <KEY, RSRC> FluentReadableResourceProvider<KEY, RSRC> from(
            final ReadableResourceProvider<? super KEY, ? extends RSRC> provider) {
        /* XXX: The casts below should be safe, since the API only consumes
         * instances of KEY and only produces instances of RSRC.
         * However, the Java generic wildcards don't want to cooperate.
         */
        if (provider instanceof FluentReadableResourceProvider<?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentReadableResourceProvider<KEY, RSRC> typedProvider
                    = (FluentReadableResourceProvider<KEY, RSRC>) provider;
            return typedProvider;
        } else {
            return new FluentReadableResourceProvider<KEY, RSRC>() {
                @Override
                public FluentReadableResource<RSRC> get(final KEY key) {
                    final ReadableResource<RSRC> resource
                            = (ReadableResource<RSRC>) provider.get(key);
                    return FluentReadableResource.from(resource);
                }
            };
        }
    }

    public <R> FluentReadableResourceProvider<KEY, R> map(
            final Func1<? super RSRC, ? extends R> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentReadableResourceProvider<KEY, R> result = new FluentReadableResourceProvider<KEY, R>() {
            @Override
            public FluentReadableResource<R> get(final KEY key) {
                final ReadableResource<? extends RSRC> resource = outerProvider().get(key);
                final ReadableResource<R> mapped = FluentReadableResource.from(resource).mapValue(mapper);
                return FluentReadableResource.from(mapped);
            }
        };
        return result;
    }

    public <K> FluentReadableResourceProvider<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentReadableResourceProvider<K, RSRC> result = new FluentReadableResourceProvider<K, RSRC>() {
            @Override
            public FluentReadableResource<RSRC> get(final K key) {
                final KEY transformedKey = adapter.call(key);
                final ReadableResource<RSRC> resource = outerProvider().get(transformedKey);
                return FluentReadableResource.from(resource);
            }
        };
        return result;
    }

    @Override
    public abstract FluentReadableResource<RSRC> get(KEY key);

    private FluentReadableResourceProvider<KEY, RSRC> outerProvider() {
        return this;
    }

}
