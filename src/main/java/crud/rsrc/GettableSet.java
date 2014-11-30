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

import crud.spi.GettableSetSpec;
import rx.Observable;
import rx.functions.Func1;


public abstract class GettableSet<KEY, RSRC> implements GettableSetSpec<KEY, RSRC> {

    public static <KEY, RSRC> GettableSet<KEY, RSRC> from(
            final GettableSetSpec<KEY, RSRC> provider) {
        if (provider instanceof GettableSet<?, ?>) {
            return (GettableSet<KEY, RSRC>) provider;
        } else {
            return new GettableSet<KEY, RSRC>() {
                @Override
                public Gettable<RSRC> getter(final KEY key) {
                    return Gettable.from(provider.getter(key));
                }
            };
        }
    }

    public <R> GettableSet<KEY, R> mapValue(
            final Func1<? super Observable<RSRC>, ? extends Observable<R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final GettableSet<KEY, R> result = new GettableSet<KEY, R>() {
            @Override
            public Gettable<R> getter(final KEY key) {
                return outerResourceSet()
                        .getter(key)
                        .mapValue(mapper);
            }
        };
        return result;
    }

    public <K> GettableSet<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final GettableSet<K, RSRC> result = new GettableSet<K, RSRC>() {
            @Override
            public Gettable<RSRC> getter(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerResourceSet().getter(transformedKey);
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

    private GettableSet<KEY, RSRC> outerResourceSet() {
        return this;
    }

}
