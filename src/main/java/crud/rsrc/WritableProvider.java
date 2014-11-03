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

import crud.spi.WritableProviderSpec;
import crud.spi.WritableSpec;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class WritableProvider<KEY, RSRC, RESPONSE>
implements WritableProviderSpec<KEY, RSRC, RESPONSE> {

    public static <KEY, RSRC, RESPONSE> WritableProvider<KEY, RSRC, RESPONSE> from(
            final WritableProviderSpec<KEY, RSRC, RESPONSE> provider) {
        if (provider instanceof WritableProvider<?, ?, ?>) {
            return (WritableProvider<KEY, RSRC, RESPONSE>) provider;
        } else {
            return new WritableProvider<KEY, RSRC, RESPONSE>() {
                @Override
                public Writable<RSRC, RESPONSE> get(final KEY key) {
                    return Writable.from(provider.get(key));
                }
            };
        }
    }

    public <RESP> WritableProvider<KEY, RSRC, RESP> mapResponse(
            final Func1<? super RESPONSE, ? extends RESP> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final WritableProvider<KEY, RSRC, RESP> result = new WritableProvider<KEY, RSRC, RESP>() {
            @Override
            public Writable<RSRC, RESP> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <R> WritableProvider<KEY, RSRC, R> flatMapResponse(
            final Func1<? super RESPONSE, ? extends Observable<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final WritableProvider<KEY, RSRC, R> result = new WritableProvider<KEY, RSRC, R>() {
            @Override
            public Writable<RSRC, R> get(final KEY key) {
                return outerProvider()
                        .get(key)
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
    public <TO> WritableProvider<KEY, RSRC, TO> flattenResponseToCompletion() {
        final MapToEmptyFunction<RESPONSE, TO> func = MapToEmptyFunction.create();
        return flatMapResponse(func);
    }

    public <RC> WritableProvider<KEY, RC, RESPONSE> adaptNewValue(
            final Func1<? super RC, ? extends RSRC> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final WritableProvider<KEY, RC, RESPONSE> result = new WritableProvider<KEY, RC, RESPONSE>() {
            @Override
            public Writable<RC, RESPONSE> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .<RC>adaptNewValue(adapter);
            }
        };
        return result;
    }

    public <K> WritableProvider<K, RSRC, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final WritableProvider<K, RSRC, RESPONSE> result = new WritableProvider<K, RSRC, RESPONSE>() {
            @Override
            public Writable<RSRC, RESPONSE> get(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerProvider().get(transformedKey);
            }
        };
        return result;
    }

    /**
     * Return a resource provider, the resource from which will transparently
     * retry calls to {@link WritableSpec#write(Object)} that throw, as
     * with {@link Observable#retry(long)}. Specifically, any
     * {@link Observable} returned by {@link WritableSpec#write(Object)}
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
    public WritableProvider<KEY, RSRC, RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new WritableProvider<KEY, RSRC, RESPONSE>() {
                @Override
                public Writable<RSRC, RESPONSE> get(final KEY key) {
                    return outerProvider()
                            .get(key)
                            .retry(maxRetries);
                }
            };
        }
    }

    public <TO> WritableProvider<KEY, RSRC, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Objects.requireNonNull(bind, "null operator");
        return new WritableProvider<KEY, RSRC, TO>() {
            @Override
            public Writable<RSRC, TO> get(final KEY key) {
                final Writable<RSRC, TO> resource = outerProvider()
                        .get(key)
                        .lift(bind);
                return resource;
            }
        };
    }

    public Func1<KEY, Writable<RSRC, RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, Writable<RSRC, RESPONSE>>(this) {
            @Override
            public Writable<RSRC, RESPONSE> call(final KEY key) {
                return get(key);
            }
        };
    }

    @Override
    public abstract Writable<RSRC, RESPONSE> get(KEY key);

    private WritableProvider<KEY, RSRC, RESPONSE> outerProvider() {
        return this;
    }

}
