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

import crud.spi.DeletableProviderSpec;
import crud.spi.Resource;
import crud.spi.ResourceProviderSpec;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations and utilities on
 * {@link DeletableProviderSpec}s.
 */
public abstract class DeletableProvider<KEY, RESPONSE>
implements DeletableProviderSpec<KEY, RESPONSE> {

    /**
     * If the given {@link ResourceProviderSpec} is already a
     * {@link DeletableProvider}, return it. Otherwise, wrap it
     * in a new instance.
     */
    public static <KEY, RESPONSE> DeletableProvider<KEY, RESPONSE> from(
            final DeletableProviderSpec<KEY, RESPONSE> provider) {
        if (provider instanceof DeletableProvider<?, ?>) {
            return (DeletableProvider<KEY, RESPONSE>) provider;
        } else {
            return new DeletableProvider<KEY, RESPONSE>() {
                @Override
                public Deletable<RESPONSE> deleter(final KEY key) {
                    return Deletable.from(provider.deleter(key));
                }
            };
        }
    }

    /**
     * @see Deletable#mapResponse(Func1)
     */
    public <R> DeletableProvider<KEY, R> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final DeletableProvider<KEY, R> result = new DeletableProvider<KEY, R>() {
            @Override
            public Deletable<R> deleter(final KEY key) {
                return outerProvider()
                        .deleter(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    /**
     * Transform the key used to look up {@link DeletableProviderSpec}s.
     */
    public <K> DeletableProvider<K, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final DeletableProvider<K, RESPONSE> result = new DeletableProvider<K, RESPONSE>() {
            @Override
            public Deletable<RESPONSE> deleter(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerProvider().deleter(transformedKey);
            }
        };
        return result;
    }

    /**
     * Present this {@link ResourceProviderSpec} as a {@link Function} from key
     * to {@link Resource}.
     */
    public Func1<KEY, Deletable<RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, Deletable<RESPONSE>>(this) {
            @Override
            public Deletable<RESPONSE> call(final KEY key) {
                return deleter(key);
            }
        };
    }

    @Override
    public abstract Deletable<RESPONSE> deleter(KEY key);

    private DeletableProvider<KEY, RESPONSE> outerProvider() {
        return this;
    }

}
