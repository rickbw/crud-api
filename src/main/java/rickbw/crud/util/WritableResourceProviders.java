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
import rx.util.functions.Func1;


public final class WritableResourceProviders {

    public static <KEY, RSRC, FROM, TO> WritableResourceProvider<KEY, RSRC, TO> map(
            final WritableResourceProvider<? super KEY, ? super RSRC, ? extends FROM> provider,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(mapper, "null function");

        final WritableResourceProvider<KEY, RSRC, TO> result = new WritableResourceProvider<KEY, RSRC, TO>() {
            @Override
            public WritableResource<RSRC, TO> get(final KEY key) {
                final WritableResource<? super RSRC, ? extends FROM> resource = provider.get(key);
                final WritableResource<RSRC, TO> mapped = FluentWritableResource.from(resource).mapResponse(mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <KEY, FROM, TO, RESPONSE> WritableResourceProvider<KEY, TO, RESPONSE> adaptNewValue(
            final WritableResourceProvider<? super KEY, ? super FROM, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final WritableResourceProvider<KEY, TO, RESPONSE> result = new WritableResourceProvider<KEY, TO, RESPONSE>() {
            @Override
            public WritableResource<TO, RESPONSE> get(final KEY key) {
                final WritableResource<? super FROM, RESPONSE> resource = provider.get(key);
                final WritableResource<TO, RESPONSE> transformed = FluentWritableResource.from(resource).adaptNewValue(adapter);
                return transformed;
            }
        };
        return result;
    }

    public static <FROM, TO, RSRC, RESPONSE> WritableResourceProvider<TO, RSRC, RESPONSE> adaptKey(
            final WritableResourceProvider<? super FROM, RSRC, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final WritableResourceProvider<TO, RSRC, RESPONSE> result = new WritableResourceProvider<TO, RSRC, RESPONSE>() {
            @Override
            public WritableResource<RSRC, RESPONSE> get(final TO key) {
                final FROM transformedKey = adapter.call(key);
                final WritableResource<RSRC, RESPONSE> resource = provider.get(transformedKey);
                return resource;
            }
        };
        return result;
    }

    private WritableResourceProviders() {
        // prevent instantiation
    }

}
