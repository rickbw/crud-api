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

import crud.spi.ReadableProviderSpec;
import crud.spi.ReadableSpec;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class ReadableProvider<KEY, RSRC>
implements ReadableProviderSpec<KEY, RSRC> {

    public static <KEY, RSRC> ReadableProvider<KEY, RSRC> from(
            final ReadableProviderSpec<KEY, RSRC> provider) {
        if (provider instanceof ReadableProvider<?, ?>) {
            return (ReadableProvider<KEY, RSRC>) provider;
        } else {
            return new ReadableProvider<KEY, RSRC>() {
                @Override
                public Readable<RSRC> reader(final KEY key) {
                    return Readable.from(provider.reader(key));
                }
            };
        }
    }

    public <R> ReadableProvider<KEY, R> mapValue(
            final Func1<? super RSRC, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final ReadableProvider<KEY, R> result = new ReadableProvider<KEY, R>() {
            @Override
            public Readable<R> reader(final KEY key) {
                return outerProvider()
                        .reader(key)
                        .mapValue(mapper);
            }
        };
        return result;
    }

    public <R> ReadableProvider<KEY, R> flatMapValue(
            final Func1<? super RSRC, ? extends Observable<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final ReadableProvider<KEY, R> result = new ReadableProvider<KEY, R>() {
            @Override
            public Readable<R> reader(final KEY key) {
                return outerProvider()
                        .reader(key)
                        .flatMapValue(mapper);
            }
        };
        return result;
    }

    public <K> ReadableProvider<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final ReadableProvider<K, RSRC> result = new ReadableProvider<K, RSRC>() {
            @Override
            public Readable<RSRC> reader(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerProvider().reader(transformedKey);
            }
        };
        return result;
    }

    /**
     * Return a resource provider, the resource from which will transparently
     * retry calls to {@link ReadableSpec#get()} that throw, as with
     * {@link Observable#retry(long)}. Specifically, any {@link Observable}
     * returned by {@link ReadableSpec#get()} will re-subscribe up to
     * {@code maxRetries} times if {@link Observer#onError(Throwable)} is
     * called, rather than propagating that {@code onError} call.
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
    public ReadableProvider<KEY, RSRC> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new ReadableProvider<KEY, RSRC>() {
                @Override
                public Readable<RSRC> reader(final KEY key) {
                    return outerProvider()
                            .reader(key)
                            .retry(maxRetries);
                }
            };
        }
    }

    public <TO> ReadableProvider<KEY, TO> lift(final Observable.Operator<TO, RSRC> bind) {
        Objects.requireNonNull(bind, "null operator");
        return new ReadableProvider<KEY, TO>() {
            @Override
            public Readable<TO> reader(final KEY key) {
                return outerProvider()
                        .reader(key)
                        .lift(bind);
            }
        };
    }

    public Func1<KEY, Readable<RSRC>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, Readable<RSRC>>(this) {
            @Override
            public Readable<RSRC> call(final KEY key) {
                return reader(key);
            }
        };
    }

    @Override
    public abstract Readable<RSRC> reader(KEY key);

    private ReadableProvider<KEY, RSRC> outerProvider() {
        return this;
    }

}
