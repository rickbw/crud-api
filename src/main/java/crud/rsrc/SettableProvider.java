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
import crud.spi.SettableSpec;
import rx.Observable;
import rx.Observer;
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
            final Func1<? super RESPONSE, ? extends RESP> mapper) {
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

    public <R> SettableProvider<KEY, RSRC, R> flatMapResponse(
            final Func1<? super RESPONSE, ? extends Observable<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final SettableProvider<KEY, RSRC, R> result = new SettableProvider<KEY, RSRC, R>() {
            @Override
            public Settable<RSRC, R> setter(final KEY key) {
                return outerProvider()
                        .setter(key)
                        .flatMapResponse(mapper);
            }
        };
        return result;
    }

    /**
     * Swallow the response(s) on success, emitting only
     * {@link Observer#onCompleted()}. Emit any error to
     * {@link Observer#onError(Throwable)} as usual.
     */
    public <TO> SettableProvider<KEY, RSRC, TO> flattenResponseToCompletion() {
        final MapToEmptyFunction<RESPONSE, TO> func = MapToEmptyFunction.create();
        return flatMapResponse(func);
    }

    public <RC> SettableProvider<KEY, RC, RESPONSE> adaptNewValue(
            final Func1<? super RC, ? extends RSRC> adapter) {
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

    /**
     * Return a resource provider, the resource from which will transparently
     * retry calls to {@link SettableSpec#set(Observable)} that throw, as
     * with {@link Observable#retry(long)}. Specifically, any
     * {@link Observable} returned by {@link SettableSpec#set(Observable)}
     * will re-subscribe up to {@code maxRetries} times if
     * {@link Observer#onError(Throwable)} is called, rather than propagating
     * that {@code onError} call.
     *
     * If a subscription fails after emitting some number of elements via
     * {@link Observer#onNext(Object)}, those elements will be emitted again
     * on the retry. For example, if an {@code Observable} fails at first
     * after emitting {@code [1, 2]}, then succeeds the second time after
     * emitting {@code [1, 2, 3, 4, 5]}, then the complete sequence of
     * emissions would be {@code [1, 2, 1, 2, 3, 4, 5, onCompleted]}.
     *
     * @param maxRetries    number of retry attempts before failing
     */
    public SettableProvider<KEY, RSRC, RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new SettableProvider<KEY, RSRC, RESPONSE>() {
                @Override
                public Settable<RSRC, RESPONSE> setter(final KEY key) {
                    return outerProvider()
                            .setter(key)
                            .retry(maxRetries);
                }
            };
        }
    }

    public <TO> SettableProvider<KEY, RSRC, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Objects.requireNonNull(bind, "null operator");
        return new SettableProvider<KEY, RSRC, TO>() {
            @Override
            public Settable<RSRC, TO> setter(final KEY key) {
                final Settable<RSRC, TO> resource = outerProvider()
                        .setter(key)
                        .lift(bind);
                return resource;
            }
        };
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
