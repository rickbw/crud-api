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

import com.google.common.base.Function;

import crud.spi.DeletableSetSpec;
import crud.spi.Resource;
import crud.spi.ResourceSet;
import crud.util.FluentFunc1;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations and utilities on
 * {@link DeletableSetSpec}s.
 */
public abstract class DeletableSet<KEY, RESPONSE> implements DeletableSetSpec<KEY, RESPONSE> {

    /**
     * If the given {@link ResourceSet} is already a
     * {@link DeletableSet}, return it. Otherwise, wrap it
     * in a new instance.
     */
    public static <KEY, RESPONSE> DeletableSet<KEY, RESPONSE> from(
            final DeletableSetSpec<KEY, RESPONSE> provider) {
        if (provider instanceof DeletableSet<?, ?>) {
            return (DeletableSet<KEY, RESPONSE>) provider;
        } else {
            return new DeletableSet<KEY, RESPONSE>() {
                @Override
                public Deletable<RESPONSE> deleter(final KEY key) {
                    return Deletable.from(provider.deleter(key));
                }
            };
        }
    }

    public static <KEY, RESPONSE> DeletableSet<KEY, RESPONSE> from(
            final Func1<? super KEY, ? extends Deletable<RESPONSE>> provider) {
        return new DeletableSet<KEY, RESPONSE>() {
            @Override
            public Deletable<RESPONSE> deleter(final KEY key) {
                return provider.call(key);
            }
        };
    }

    /**
     * @see Deletable#mapResponse(Func1)
     */
    public <R> DeletableSet<KEY, R> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final DeletableSet<KEY, R> result = new DeletableSet<KEY, R>() {
            @Override
            public Deletable<R> deleter(final KEY key) {
                return outerResourceSet()
                        .deleter(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    /**
     * Transform the key used to look up {@link DeletableSetSpec}s.
     */
    public <K> DeletableSet<K, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final DeletableSet<K, RESPONSE> result = new DeletableSet<K, RESPONSE>() {
            @Override
            public Deletable<RESPONSE> deleter(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerResourceSet().deleter(transformedKey);
            }
        };
        return result;
    }

    /**
     * Present this {@link ResourceSet} as a {@link Function} from key
     * to {@link Resource}.
     */
    public Func1<KEY, Deletable<RESPONSE>> toFunction() {
        return FluentFunc1.from(new DelegateObjectMethods.Function<KEY, Deletable<RESPONSE>>(this) {
            @Override
            public Deletable<RESPONSE> call(final KEY key) {
                return deleter(key);
            }
        });
    }

    @Override
    public abstract Deletable<RESPONSE> deleter(KEY key);

    private DeletableSet<KEY, RESPONSE> outerResourceSet() {
        return this;
    }

}
