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

import crud.spi.UpdatableProviderSpec;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link UpdatableProviderSpec}s.
 *
 * Note that this class lacks a {@code retry} operation, as in e.g.
 * {@link ReadableProvider#retry(int)}. This is because updates
 * are not idempotent; hence, retries are not inherently retriable.
 * Applications must handle retry logic, if any, themselves.
 */
public abstract class UpdatableProvider<KEY, UPDATE, RESPONSE>
implements UpdatableProviderSpec<KEY, UPDATE, RESPONSE> {

    public static <KEY, UPDATE, RESPONSE> UpdatableProvider<KEY, UPDATE, RESPONSE> from(
            final UpdatableProviderSpec<KEY, UPDATE, RESPONSE> provider) {
        if (provider instanceof UpdatableProvider<?, ?, ?>) {
            return (UpdatableProvider<KEY, UPDATE, RESPONSE>) provider;
        } else {
            return new UpdatableProvider<KEY, UPDATE, RESPONSE>() {
                @Override
                public Updatable<UPDATE, RESPONSE> get(final KEY key) {
                    return Updatable.from(provider.get(key));
                }
            };
        }
    }

    public <R> UpdatableProvider<KEY, UPDATE, R> mapResponse(
            final Func1<? super RESPONSE, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final UpdatableProvider<KEY, UPDATE, R> result = new UpdatableProvider<KEY, UPDATE, R>() {
            @Override
            public Updatable<UPDATE, R> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <R> UpdatableProvider<KEY, UPDATE, R> flatMapResponse(
            final Func1<? super RESPONSE, ? extends Observable<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final UpdatableProvider<KEY, UPDATE, R> result = new UpdatableProvider<KEY, UPDATE, R>() {
            @Override
            public Updatable<UPDATE, R> get(final KEY key) {
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
    public <TO> UpdatableProvider<KEY, UPDATE, TO> flattenResponseToCompletion() {
        final MapToEmptyFunction<RESPONSE, TO> func = MapToEmptyFunction.create();
        return flatMapResponse(func);
    }

    public <U> UpdatableProvider<KEY, U, RESPONSE> adaptUpdate(
            final Func1<? super U, ? extends UPDATE> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final UpdatableProvider<KEY, U, RESPONSE> result = new UpdatableProvider<KEY, U, RESPONSE>() {
            @Override
            public Updatable<U, RESPONSE> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .adaptUpdate(adapter);
            }
        };
        return result;
    }

    public <K> UpdatableProvider<K, UPDATE, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final UpdatableProvider<K, UPDATE, RESPONSE> result = new UpdatableProvider<K, UPDATE, RESPONSE>() {
            @Override
            public Updatable<UPDATE, RESPONSE> get(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerProvider().get(transformedKey);
            }
        };
        return result;
    }

    public <TO> UpdatableProvider<KEY, UPDATE, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Objects.requireNonNull(bind, "null operator");
        return new UpdatableProvider<KEY, UPDATE, TO>() {
            @Override
            public Updatable<UPDATE, TO> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .lift(bind);
            }
        };
    }

    public Func1<KEY, Updatable<UPDATE, RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, Updatable<UPDATE, RESPONSE>>(this) {
            @Override
            public Updatable<UPDATE, RESPONSE> call(final KEY key) {
                return get(key);
            }
        };
    }

    @Override
    public abstract Updatable<UPDATE, RESPONSE> get(KEY key);

    private UpdatableProvider<KEY, UPDATE, RESPONSE> outerProvider() {
        return this;
    }

}
