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

import crud.spi.GettableProviderSpec;
import rx.Observable;
import rx.functions.Func1;


public abstract class GettableProvider<KEY, RSRC>
implements GettableProviderSpec<KEY, RSRC> {

    public static <KEY, RSRC> GettableProvider<KEY, RSRC> from(
            final GettableProviderSpec<KEY, RSRC> provider) {
        if (provider instanceof GettableProvider<?, ?>) {
            return (GettableProvider<KEY, RSRC>) provider;
        } else {
            return new GettableProvider<KEY, RSRC>() {
                @Override
                public Gettable<RSRC> getter(final KEY key) {
                    return Gettable.from(provider.getter(key));
                }
            };
        }
    }

    public <R> GettableProvider<KEY, R> mapValue(
            final Func1<? super Observable<RSRC>, ? extends Observable<R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final GettableProvider<KEY, R> result = new GettableProvider<KEY, R>() {
            @Override
            public Gettable<R> getter(final KEY key) {
                return outerProvider()
                        .getter(key)
                        .mapValue(mapper);
            }
        };
        return result;
    }

    public <K> GettableProvider<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final GettableProvider<K, RSRC> result = new GettableProvider<K, RSRC>() {
            @Override
            public Gettable<RSRC> getter(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerProvider().getter(transformedKey);
            }
        };
        return result;
    }

    public Func1<KEY, Gettable<RSRC>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, Gettable<RSRC>>(this) {
            @Override
            public Gettable<RSRC> call(final KEY key) {
                return getter(key);
            }
        };
    }

    @Override
    public abstract Gettable<RSRC> getter(KEY key);

    private GettableProvider<KEY, RSRC> outerProvider() {
        return this;
    }

}
