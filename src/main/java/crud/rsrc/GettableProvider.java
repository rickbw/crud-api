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

import crud.spi.GettableProviderSpec;
import crud.spi.GettableSpec;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class GettableProvider<KEY, RSRC>
implements GettableProviderSpec<KEY, RSRC> {

    public static <KEY, RSRC> GettableProvider<KEY, RSRC> from(
            final GettableProviderSpec<KEY, RSRC> provider) {
        if (provider instanceof GettableProvider<?, ?>) {
            return (GettableProvider<KEY, RSRC>) provider;
        } else {
            return new GettableProvider<KEY, RSRC>() {
                @Override
                public Gettable<RSRC> getter(final KEY key) {
                    return Gettable.from(provider.getter(key));
                }
            };
        }
    }

    public <R> GettableProvider<KEY, R> mapValue(
            final Func1<? super RSRC, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final GettableProvider<KEY, R> result = new GettableProvider<KEY, R>() {
            @Override
            public Gettable<R> getter(final KEY key) {
                return outerProvider()
                        .getter(key)
                        .mapValue(mapper);
            }
        };
        return result;
    }

    public <R> GettableProvider<KEY, R> flatMapValue(
            final Func1<? super RSRC, ? extends Observable<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final GettableProvider<KEY, R> result = new GettableProvider<KEY, R>() {
            @Override
            public Gettable<R> getter(final KEY key) {
                return outerProvider()
                        .getter(key)
                        .flatMapValue(mapper);
            }
        };
        return result;
    }

    public <K> GettableProvider<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final GettableProvider<K, RSRC> result = new GettableProvider<K, RSRC>() {
            @Override
            public Gettable<RSRC> getter(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerProvider().getter(transformedKey);
            }
        };
        return result;
    }

    /**
     * Return a resource provider, the resource from which will transparently
     * retry calls to {@link GettableSpec#get()} that throw, as with
     * {@link Observable#retry(long)}. Specifically, any {@link Observable}
     * returned by {@link GettableSpec#get()} will re-subscribe up to
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
    public GettableProvider<KEY, RSRC> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new GettableProvider<KEY, RSRC>() {
                @Override
                public Gettable<RSRC> getter(final KEY key) {
                    return outerProvider()
                            .getter(key)
                            .retry(maxRetries);
                }
            };
        }
    }

    public <TO> GettableProvider<KEY, TO> lift(final Observable.Operator<TO, RSRC> bind) {
        Objects.requireNonNull(bind, "null operator");
        return new GettableProvider<KEY, TO>() {
            @Override
            public Gettable<TO> getter(final KEY key) {
                return outerProvider()
                        .getter(key)
                        .lift(bind);
            }
        };
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

    private GettableProvider<KEY, RSRC> outerProvider() {
        return this;
    }

}
