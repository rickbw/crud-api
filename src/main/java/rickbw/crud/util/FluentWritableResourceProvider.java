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

import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;
import rx.functions.Func1;


public abstract class FluentWritableResourceProvider<KEY, RSRC, RESPONSE>
implements WritableResourceProvider<KEY, RSRC, RESPONSE> {

    public static <KEY, RSRC, RESPONSE> FluentWritableResourceProvider<KEY, RSRC, RESPONSE> from(
            final WritableResourceProvider<? super KEY, ? super RSRC, ? extends RESPONSE> provider) {
        /* XXX: The casts below should be safe, since the API only consumes
         * instances of KEY and RSRC and only produces instances of RESPONSE.
         * However, the Java generic wildcards don't want to cooperate.
         */
        if (provider instanceof FluentWritableResourceProvider<?, ?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentWritableResourceProvider<KEY, RSRC, RESPONSE> typedProvider
                    = (FluentWritableResourceProvider<KEY, RSRC, RESPONSE>) provider;
            return typedProvider;
        } else {
            return new FluentWritableResourceProvider<KEY, RSRC, RESPONSE>() {
                @Override
                public FluentWritableResource<RSRC, RESPONSE> get(final KEY key) {
                    final WritableResource<RSRC, RESPONSE> rsrc
                            = (WritableResource<RSRC, RESPONSE>) provider.get(key);
                    return FluentWritableResource.from(rsrc);
                }
            };
        }
    }

    public <RESP> FluentWritableResourceProvider<KEY, RSRC, RESP> map(
            final Func1<? super RESPONSE, ? extends RESP> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentWritableResourceProvider<KEY, RSRC, RESP> result = new FluentWritableResourceProvider<KEY, RSRC, RESP>() {
            @Override
            public FluentWritableResource<RSRC, RESP> get(final KEY key) {
                final WritableResource<? super RSRC, ? extends RESPONSE> resource = outerProvider().get(key);
                final WritableResource<RSRC, RESP> mapped = FluentWritableResource.from(resource).mapResponse(mapper);
                return FluentWritableResource.from(mapped);
            }
        };
        return result;
    }

    public <RC> FluentWritableResourceProvider<KEY, RC, RESPONSE> adaptNewValue(
            final Func1<? super RC, ? extends RSRC> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentWritableResourceProvider<KEY, RC, RESPONSE> result = new FluentWritableResourceProvider<KEY, RC, RESPONSE>() {
            @Override
            public FluentWritableResource<RC, RESPONSE> get(final KEY key) {
                final WritableResource<? super RSRC, RESPONSE> resource = outerProvider().get(key);
                final WritableResource<RC, RESPONSE> transformed = FluentWritableResource.from(resource).adaptNewValue(adapter);
                return FluentWritableResource.from(transformed);
            }
        };
        return result;
    }

    public <K> FluentWritableResourceProvider<K, RSRC, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentWritableResourceProvider<K, RSRC, RESPONSE> result = new FluentWritableResourceProvider<K, RSRC, RESPONSE>() {
            @Override
            public FluentWritableResource<RSRC, RESPONSE> get(final K key) {
                final KEY transformedKey = adapter.call(key);
                final WritableResource<RSRC, RESPONSE> resource = outerProvider().get(transformedKey);
                return FluentWritableResource.from(resource);
            }
        };
        return result;
    }

    @Override
    public abstract FluentWritableResource<RSRC, RESPONSE> get(KEY key);

    private FluentWritableResourceProvider<KEY, RSRC, RESPONSE> outerProvider() {
        return this;
    }

}
