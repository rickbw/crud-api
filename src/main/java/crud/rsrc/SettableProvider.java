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

import crud.spi.SettableProviderSpec;
import rx.Observable;
import rx.functions.Func1;


public abstract class SettableProvider<KEY, RSRC, RESPONSE>
implements SettableProviderSpec<KEY, RSRC, RESPONSE> {

    public static <KEY, RSRC, RESPONSE> SettableProvider<KEY, RSRC, RESPONSE> from(
            final SettableProviderSpec<KEY, RSRC, RESPONSE> provider) {
        if (provider instanceof SettableProvider<?, ?, ?>) {
            return (SettableProvider<KEY, RSRC, RESPONSE>) provider;
        } else {
            return new SettableProvider<KEY, RSRC, RESPONSE>() {
                @Override
                public Settable<RSRC, RESPONSE> setter(final KEY key) {
                    return Settable.from(provider.setter(key));
                }
            };
        }
    }

    public <RESP> SettableProvider<KEY, RSRC, RESP> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<RESP>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final SettableProvider<KEY, RSRC, RESP> result = new SettableProvider<KEY, RSRC, RESP>() {
            @Override
            public Settable<RSRC, RESP> setter(final KEY key) {
                return outerProvider()
                        .setter(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <RC> SettableProvider<KEY, RC, RESPONSE> adaptNewValue(
            final Func1<? super Observable<RC>, ? extends Observable<RSRC>> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final SettableProvider<KEY, RC, RESPONSE> result = new SettableProvider<KEY, RC, RESPONSE>() {
            @Override
            public Settable<RC, RESPONSE> setter(final KEY key) {
                return outerProvider()
                        .setter(key)
                        .<RC>adaptNewValue(adapter);
            }
        };
        return result;
    }

    public <K> SettableProvider<K, RSRC, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final SettableProvider<K, RSRC, RESPONSE> result = new SettableProvider<K, RSRC, RESPONSE>() {
            @Override
            public Settable<RSRC, RESPONSE> setter(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerProvider().setter(transformedKey);
            }
        };
        return result;
    }

    public Func1<KEY, Settable<RSRC, RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, Settable<RSRC, RESPONSE>>(this) {
            @Override
            public Settable<RSRC, RESPONSE> call(final KEY key) {
                return setter(key);
            }
        };
    }

    @Override
    public abstract Settable<RSRC, RESPONSE> setter(KEY key);

    private SettableProvider<KEY, RSRC, RESPONSE> outerProvider() {
        return this;
    }

}
