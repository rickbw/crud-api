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

import crud.spi.ResourceSet;
import crud.spi.SettableSetSpec;
import crud.util.FluentFunc1;
import rx.Observable;
import rx.functions.Func1;


public abstract class SettableSet<KEY, RSRC, RESPONSE>
implements SettableSetSpec<KEY, RSRC, RESPONSE> {

    public static <KEY, RSRC, RESPONSE> SettableSet<KEY, RSRC, RESPONSE> from(
            final SettableSetSpec<KEY, RSRC, RESPONSE> provider) {
        if (provider instanceof SettableSet<?, ?, ?>) {
            return (SettableSet<KEY, RSRC, RESPONSE>) provider;
        } else {
            return new SettableSet<KEY, RSRC, RESPONSE>() {
                @Override
                public Settable<RSRC, RESPONSE> setter(final KEY key) {
                    return Settable.from(provider.setter(key));
                }
            };
        }
    }

    public static <KEY, RSRC, RESPONSE> SettableSet<KEY, RSRC, RESPONSE> from(
            final Func1<? super KEY, ? extends Settable<RSRC, RESPONSE>> provider) {
        return new SettableSet<KEY, RSRC, RESPONSE>() {
            @Override
            public Settable<RSRC, RESPONSE> setter(final KEY key) {
                return provider.call(key);
            }
        };
    }

    public <RESP> SettableSet<KEY, RSRC, RESP> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<RESP>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final SettableSet<KEY, RSRC, RESP> result = new SettableSet<KEY, RSRC, RESP>() {
            @Override
            public Settable<RSRC, RESP> setter(final KEY key) {
                return outerResourceSet()
                        .setter(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <RC> SettableSet<KEY, RC, RESPONSE> adaptNewValue(
            final Func1<? super Observable<RC>, ? extends Observable<RSRC>> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final SettableSet<KEY, RC, RESPONSE> result = new SettableSet<KEY, RC, RESPONSE>() {
            @Override
            public Settable<RC, RESPONSE> setter(final KEY key) {
                return outerResourceSet()
                        .setter(key)
                        .<RC>adaptNewValue(adapter);
            }
        };
        return result;
    }

    public <K> SettableSet<K, RSRC, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final SettableSet<K, RSRC, RESPONSE> result = new SettableSet<K, RSRC, RESPONSE>() {
            @Override
            public Settable<RSRC, RESPONSE> setter(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerResourceSet().setter(transformedKey);
            }
        };
        return result;
    }

    public FluentFunc1<KEY, Settable<RSRC, RESPONSE>> toFunction() {
        return FluentFunc1.from(new DelegateObjectMethods.Function<KEY, Settable<RSRC, RESPONSE>>(this) {
            @Override
            public Settable<RSRC, RESPONSE> call(final KEY key) {
                return setter(key);
            }
        });
    }

    /**
     * Present this {@code SettableSet} as an {@link UpdatableSet}. All
     * updates made via {@link Updatable#update(Observable)} will be reflected
     * in the state of a corresponding {@link Settable} resource, as if
     * {@link Settable#set(Observable)} had been called instead.
     *
     * Viewing this {@link ResourceSet} as an {@code UpdatableSet} is safe,
     * because the contract of {@code Updatable} is more relaxed than that of
     * {@code Settable}. (The opposite is not true, and you will find no
     * {@code toSettableSet} operation on {@code UpdatableSet}.)
     */
    public UpdatableSet<KEY, RSRC, RESPONSE> toUpdatableSet() {
        return UpdatableSet.from(new Func1<KEY, Updatable<RSRC, RESPONSE>>() {
            @Override
            public Updatable<RSRC, RESPONSE> call(final KEY key) {
                return setter(key).toUpdater();
            }
        });
    }

    @Override
    public abstract Settable<RSRC, RESPONSE> setter(KEY key);

    private SettableSet<KEY, RSRC, RESPONSE> outerResourceSet() {
        return this;
    }

}
