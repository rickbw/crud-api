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

import crud.spi.UpdatableSetSpec;
import crud.util.FluentFunc1;
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link UpdatableSetSpec}s.
 */
public abstract class UpdatableSet<KEY, UPDATE, RESPONSE>
implements UpdatableSetSpec<KEY, UPDATE, RESPONSE> {

    public static <KEY, UPDATE, RESPONSE> UpdatableSet<KEY, UPDATE, RESPONSE> from(
            final UpdatableSetSpec<KEY, UPDATE, RESPONSE> provider) {
        if (provider instanceof UpdatableSet<?, ?, ?>) {
            return (UpdatableSet<KEY, UPDATE, RESPONSE>) provider;
        } else {
            return new UpdatableSet<KEY, UPDATE, RESPONSE>() {
                @Override
                public Updatable<UPDATE, RESPONSE> updater(final KEY key) {
                    return Updatable.from(provider.updater(key));
                }
            };
        }
    }

    public static <KEY, UPDATE, RESPONSE> UpdatableSet<KEY, UPDATE, RESPONSE> from(
            final Func1<? super KEY, ? extends Updatable<UPDATE, RESPONSE>> provider) {
        return new UpdatableSet<KEY, UPDATE, RESPONSE>() {
            @Override
            public Updatable<UPDATE, RESPONSE> updater(final KEY key) {
                return provider.call(key);
            }
        };
    }

    public <R> UpdatableSet<KEY, UPDATE, R> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final UpdatableSet<KEY, UPDATE, R> result = new UpdatableSet<KEY, UPDATE, R>() {
            @Override
            public Updatable<UPDATE, R> updater(final KEY key) {
                return outerResourceSet()
                        .updater(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <U> UpdatableSet<KEY, U, RESPONSE> adaptUpdate(
            final Func1<? super Observable<U>, ? extends Observable<UPDATE>> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final UpdatableSet<KEY, U, RESPONSE> result = new UpdatableSet<KEY, U, RESPONSE>() {
            @Override
            public Updatable<U, RESPONSE> updater(final KEY key) {
                return outerResourceSet()
                        .updater(key)
                        .adaptUpdate(adapter);
            }
        };
        return result;
    }

    public <K> UpdatableSet<K, UPDATE, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final UpdatableSet<K, UPDATE, RESPONSE> result = new UpdatableSet<K, UPDATE, RESPONSE>() {
            @Override
            public Updatable<UPDATE, RESPONSE> updater(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerResourceSet().updater(transformedKey);
            }
        };
        return result;
    }

    public FluentFunc1<KEY, Updatable<UPDATE, RESPONSE>> toFunction() {
        return FluentFunc1.from(new DelegateObjectMethods.Function<KEY, Updatable<UPDATE, RESPONSE>>(this) {
            @Override
            public Updatable<UPDATE, RESPONSE> call(final KEY key) {
                return updater(key);
            }
        });
    }

    @Override
    public abstract Updatable<UPDATE, RESPONSE> updater(KEY key);

    private UpdatableSet<KEY, UPDATE, RESPONSE> outerResourceSet() {
        return this;
    }

}
