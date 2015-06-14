/* Copyright 2013–2015 Rick Warren
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
package crud.transform;

import java.util.Objects;

import crud.core.ReadableResourceSet;
import rx.Observable;
import rx.functions.Func1;


public abstract class FluentReadableResourceSet<KEY, RSRC>
implements ReadableResourceSet<KEY, RSRC> {

    public static <KEY, RSRC> FluentReadableResourceSet<KEY, RSRC> from(
            final ReadableResourceSet<KEY, RSRC> rsrcSet) {
        if (rsrcSet instanceof FluentReadableResourceSet<?, ?>) {
            return (FluentReadableResourceSet<KEY, RSRC>) rsrcSet;
        } else {
            return new FluentReadableResourceSet<KEY, RSRC>() {
                @Override
                public FluentReadableResource<RSRC> get(final KEY key) {
                    return FluentReadableResource.from(rsrcSet.get(key));
                }
            };
        }
    }

    public <R> FluentReadableResourceSet<KEY, R> mapValue(
            final Func1<? super Observable<? super RSRC>, ? extends Observable<R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final FluentReadableResourceSet<KEY, R> result = new FluentReadableResourceSet<KEY, R>() {
            @Override
            public FluentReadableResource<R> get(final KEY key) {
                return outerResourceSet()
                        .get(key)
                        .mapValue(mapper);
            }
        };
        return result;
    }

    public <K> FluentReadableResourceSet<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final FluentReadableResourceSet<K, RSRC> result = new FluentReadableResourceSet<K, RSRC>() {
            @Override
            public FluentReadableResource<RSRC> get(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerResourceSet().get(transformedKey);
            }
        };
        return result;
    }

    public Func1<KEY, FluentReadableResource<RSRC>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, FluentReadableResource<RSRC>>(this) {
            @Override
            public FluentReadableResource<RSRC> call(final KEY key) {
                return get(key);
            }
        };
    }

    @Override
    public abstract FluentReadableResource<RSRC> get(KEY key);

    private FluentReadableResourceSet<KEY, RSRC> outerResourceSet() {
        return this;
    }

}
