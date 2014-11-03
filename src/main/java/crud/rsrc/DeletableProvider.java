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
import crud.spi.DeletableSpec;
import crud.spi.Resource;
import crud.spi.ResourceProviderSpec;
import rx.Observable;
import rx.Observer;
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
                public Deletable<RESPONSE> get(final KEY key) {
                    return Deletable.from(provider.get(key));
                }
            };
        }
    }

    /**
     * @see Deletable#mapResponse(Func1)
     */
    public <R> DeletableProvider<KEY, R> mapResponse(
            final Func1<? super RESPONSE, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final DeletableProvider<KEY, R> result = new DeletableProvider<KEY, R>() {
            @Override
            public Deletable<R> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <R> DeletableProvider<KEY, R> flatMapResponse(
            final Func1<? super RESPONSE, ? extends Observable<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final DeletableProvider<KEY, R> result = new DeletableProvider<KEY, R>() {
            @Override
            public Deletable<R> get(final KEY key) {
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
    public <TO> DeletableProvider<KEY, TO> flattenResponseToCompletion() {
        final MapToEmptyFunction<RESPONSE, TO> func = MapToEmptyFunction.create();
        return flatMapResponse(func);
    }

    /**
     * Transform the key used to look up {@link DeletableProviderSpec}s.
     */
    public <K> DeletableProvider<K, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final DeletableProvider<K, RESPONSE> result = new DeletableProvider<K, RESPONSE>() {
            @Override
            public Deletable<RESPONSE> get(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerProvider().get(transformedKey);
            }
        };
        return result;
    }

    /**
     * Return a resource provider, the resource from which will transparently
     * retry calls to {@link DeletableSpec#delete()} that throw, as with
     * {@link Observable#retry(long)}. Specifically, any {@link Observable}
     * returned by {@link DeletableSpec#delete()} will re-subscribe up to
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
     *
     * @see Deletable#retry(int)
     */
    public DeletableProvider<KEY, RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new DeletableProvider<KEY, RESPONSE>() {
                @Override
                public Deletable<RESPONSE> get(final KEY key) {
                    return outerProvider()
                            .get(key)
                            .retry(maxRetries);
                }
            };
        }
    }

    /**
     * @see Deletable#lift(rx.Observable.Operator)
     */
    public <TO> DeletableProvider<KEY, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Objects.requireNonNull(bind, "null operator");
        return new DeletableProvider<KEY, TO>() {
            @Override
            public Deletable<TO> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .lift(bind);
            }
        };
    }

    /**
     * Present this {@link ResourceProviderSpec} as a {@link Function} from key
     * to {@link Resource}.
     */
    public Func1<KEY, Deletable<RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, Deletable<RESPONSE>>(this) {
            @Override
            public Deletable<RESPONSE> call(final KEY key) {
                return get(key);
            }
        };
    }

    @Override
    public abstract Deletable<RESPONSE> get(KEY key);

    private DeletableProvider<KEY, RESPONSE> outerProvider() {
        return this;
    }

}
