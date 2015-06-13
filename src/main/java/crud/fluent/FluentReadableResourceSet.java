/* Copyright 2013–2015 Rick Warren
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
package crud.fluent;

import java.util.Objects;

import crud.core.ReadableResource;
import crud.core.ReadableResourceSet;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class FluentReadableResourceSet<KEY, RSRC>
implements ReadableResourceSet<KEY, RSRC> {

    public static <KEY, RSRC> FluentReadableResourceSet<KEY, RSRC> from(
            final ReadableResourceSet<KEY, RSRC> rsrcSet) {
        if (rsrcSet instanceof FluentReadableResourceSet<?, ?>) {
            return (FluentReadableResourceSet<KEY, RSRC>) rsrcSet;
        } else {
            return new FluentReadableResourceSet<KEY, RSRC>() {
                @Override
                public FluentReadableResource<RSRC> get(final KEY key) {
                    return FluentReadableResource.from(rsrcSet.get(key));
                }
            };
        }
    }

    public <R> FluentReadableResourceSet<KEY, R> mapValue(
            final Func1<? super RSRC, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final FluentReadableResourceSet<KEY, R> result = new FluentReadableResourceSet<KEY, R>() {
            @Override
            public FluentReadableResource<R> get(final KEY key) {
                return outerResourceSet()
                        .get(key)
                        .mapValue(mapper);
            }
        };
        return result;
    }

    public <R> FluentReadableResourceSet<KEY, R> flatMapValue(
            final Func1<? super RSRC, ? extends Observable<? extends R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final FluentReadableResourceSet<KEY, R> result = new FluentReadableResourceSet<KEY, R>() {
            @Override
            public FluentReadableResource<R> get(final KEY key) {
                return outerResourceSet()
                        .get(key)
                        .flatMapValue(mapper);
            }
        };
        return result;
    }

    public <K> FluentReadableResourceSet<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final FluentReadableResourceSet<K, RSRC> result = new FluentReadableResourceSet<K, RSRC>() {
            @Override
            public FluentReadableResource<RSRC> get(final K key) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerResourceSet().get(transformedKey);
            }
        };
        return result;
    }

    /**
     * Return a resource set, the resource from which will transparently
     * retry calls to {@link ReadableResource#read()} that throw, as with
     * {@link Observable#retry(long)}. Specifically, any {@link Observable}
     * returned by {@link ReadableResource#read()} will re-subscribe up to
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
    public FluentReadableResourceSet<KEY, RSRC> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new FluentReadableResourceSet<KEY, RSRC>() {
                @Override
                public FluentReadableResource<RSRC> get(final KEY key) {
                    return outerResourceSet()
                            .get(key)
                            .retry(maxRetries);
                }
            };
        }
    }

    public <TO> FluentReadableResourceSet<KEY, TO> lift(final Observable.Operator<TO, RSRC> bind) {
        Objects.requireNonNull(bind, "null operator");
        return new FluentReadableResourceSet<KEY, TO>() {
            @Override
            public FluentReadableResource<TO> get(final KEY key) {
                return outerResourceSet()
                        .get(key)
                        .lift(bind);
            }
        };
    }

    public Func1<KEY, FluentReadableResource<RSRC>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, FluentReadableResource<RSRC>>(this) {
            @Override
            public FluentReadableResource<RSRC> call(final KEY key) {
                return get(key);
            }
        };
    }

    @Override
    public abstract FluentReadableResource<RSRC> get(KEY key);

    private FluentReadableResourceSet<KEY, RSRC> outerResourceSet() {
        return this;
    }

}
