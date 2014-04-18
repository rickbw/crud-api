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
package rickbw.crud.fluent;

import java.util.Objects;

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class FluentDeletableResourceProvider<KEY, RESPONSE>
implements DeletableResourceProvider<KEY, RESPONSE> {

    public static <KEY, RESPONSE> FluentDeletableResourceProvider<KEY, RESPONSE> from(
            final DeletableResourceProvider<KEY, RESPONSE> provider) {
        if (provider instanceof FluentDeletableResourceProvider<?, ?>) {
            return (FluentDeletableResourceProvider<KEY, RESPONSE>) provider;
        } else {
            return new FluentDeletableResourceProvider<KEY, RESPONSE>() {
                @Override
                public FluentDeletableResource<RESPONSE> get(final KEY key) {
                    return FluentDeletableResource.from(provider.get(key));
                }
            };
        }
    }

    public <R> FluentDeletableResourceProvider<KEY, R> mapResponse(
            final Func1<? super RESPONSE, ? extends R> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final FluentDeletableResourceProvider<KEY, R> result = new FluentDeletableResourceProvider<KEY, R>() {
            @Override
            public FluentDeletableResource<R> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <K> FluentDeletableResourceProvider<K, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final FluentDeletableResourceProvider<K, RESPONSE> result = new FluentDeletableResourceProvider<K, RESPONSE>() {
            @Override
            public FluentDeletableResource<RESPONSE> get(final K key) {
                final KEY transformedKey = adapter.call(key);
                return outerProvider().get(transformedKey);
            }
        };
        return result;
    }

    /**
     * Return a resource provider, the resource from which will transparently
     * retry calls to {@link DeletableResource#delete()} that throw, as with
     * {@link Observable#retry(int)}. Specifically, any {@link Observable}
     * returned by {@link DeletableResource#delete()} will re-subscribe up to
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
    public FluentDeletableResourceProvider<KEY, RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new FluentDeletableResourceProvider<KEY, RESPONSE>() {
                @Override
                public FluentDeletableResource<RESPONSE> get(final KEY key) {
                    return outerProvider()
                            .get(key)
                            .retry(maxRetries);
                }
            };
        }
    }

    public <TO> FluentDeletableResourceProvider<KEY, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Objects.requireNonNull(bind, "null operator");
        return new FluentDeletableResourceProvider<KEY, TO>() {
            @Override
            public FluentDeletableResource<TO> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .lift(bind);
            }
        };
    }

    public Func1<KEY, FluentDeletableResource<RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, FluentDeletableResource<RESPONSE>>(this) {
            @Override
            public FluentDeletableResource<RESPONSE> call(final KEY key) {
                return get(key);
            }
        };
    }

    @Override
    public abstract FluentDeletableResource<RESPONSE> get(KEY key);

    private FluentDeletableResourceProvider<KEY, RESPONSE> outerProvider() {
        return this;
    }

}
