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
import rx.util.functions.Func1;


public final class UpdatableResourceProviders {

    public static <KEY, UPDATE, FROM, TO> UpdatableResourceProvider<KEY, UPDATE, TO> map(
            final UpdatableResourceProvider<? super KEY, ? super UPDATE, ? extends FROM> provider,
            final Func1<? super FROM, ? extends TO> mapper) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(mapper, "null function");

        final UpdatableResourceProvider<KEY, UPDATE, TO> result = new UpdatableResourceProvider<KEY, UPDATE, TO>() {
            @Override
            public UpdatableResource<UPDATE, TO> get(final KEY key) {
                final UpdatableResource<? super UPDATE, ? extends FROM> resource = provider.get(key);
                final UpdatableResource<UPDATE, TO> mapped = FluentUpdatableResource.from(resource).mapResponse(mapper);
                return mapped;
            }
        };
        return result;
    }

    public static <KEY, FROM, TO, RESPONSE> UpdatableResourceProvider<KEY, TO, RESPONSE> adaptUpdate(
            final UpdatableResourceProvider<? super KEY, ? super FROM, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final UpdatableResourceProvider<KEY, TO, RESPONSE> result = new UpdatableResourceProvider<KEY, TO, RESPONSE>() {
            @Override
            public UpdatableResource<TO, RESPONSE> get(final KEY key) {
                final UpdatableResource<? super FROM, RESPONSE> resource = provider.get(key);
                final UpdatableResource<TO, RESPONSE> transformed = FluentUpdatableResource.from(resource).adaptUpdate(adapter);
                return transformed;
            }
        };
        return result;
    }

    public static <FROM, TO, UPDATE, RESPONSE> UpdatableResourceProvider<TO, UPDATE, RESPONSE> adaptKey(
            final UpdatableResourceProvider<? super FROM, UPDATE, RESPONSE> provider,
            final Func1<? super TO, ? extends FROM> adapter) {
        Preconditions.checkNotNull(provider, "null provider");
        Preconditions.checkNotNull(adapter, "null function");

        final UpdatableResourceProvider<TO, UPDATE, RESPONSE> result = new UpdatableResourceProvider<TO, UPDATE, RESPONSE>() {
            @Override
            public UpdatableResource<UPDATE, RESPONSE> get(final TO key) {
                final FROM transformedKey = adapter.call(key);
                final UpdatableResource<UPDATE, RESPONSE> resource = provider.get(transformedKey);
                return resource;
            }
        };
        return result;
    }

    private UpdatableResourceProviders() {
        // prevent instantiation
    }

}
