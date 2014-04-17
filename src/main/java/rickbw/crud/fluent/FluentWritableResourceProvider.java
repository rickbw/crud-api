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

import com.google.common.base.Preconditions;

import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class FluentWritableResourceProvider<KEY, RSRC, RESPONSE>
implements WritableResourceProvider<KEY, RSRC, RESPONSE> {

    public static <KEY, RSRC, RESPONSE> FluentWritableResourceProvider<KEY, RSRC, RESPONSE> from(
            final WritableResourceProvider<KEY, RSRC, RESPONSE> provider) {
        if (provider instanceof FluentWritableResourceProvider<?, ?, ?>) {
            return (FluentWritableResourceProvider<KEY, RSRC, RESPONSE>) provider;
        } else {
            return new FluentWritableResourceProvider<KEY, RSRC, RESPONSE>() {
                @Override
                public FluentWritableResource<RSRC, RESPONSE> get(final KEY key) {
                    return FluentWritableResource.from(provider.get(key));
                }
            };
        }
    }

    public <RESP> FluentWritableResourceProvider<KEY, RSRC, RESP> mapResponse(
            final Func1<? super RESPONSE, ? extends RESP> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentWritableResourceProvider<KEY, RSRC, RESP> result = new FluentWritableResourceProvider<KEY, RSRC, RESP>() {
            @Override
            public FluentWritableResource<RSRC, RESP> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <RC> FluentWritableResourceProvider<KEY, RC, RESPONSE> adaptNewValue(
            final Func1<? super RC, ? extends RSRC> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentWritableResourceProvider<KEY, RC, RESPONSE> result = new FluentWritableResourceProvider<KEY, RC, RESPONSE>() {
            @Override
            public FluentWritableResource<RC, RESPONSE> get(final KEY key) {
                return outerProvider()
                        .get(key)
                        .<RC>adaptNewValue(adapter);
            }
        };
        return result;
    }

    public <K> FluentWritableResourceProvider<K, RSRC, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentWritableResourceProvider<K, RSRC, RESPONSE> result = new FluentWritableResourceProvider<K, RSRC, RESPONSE>() {
            @Override
            public FluentWritableResource<RSRC, RESPONSE> get(final K key) {
                final KEY transformedKey = adapter.call(key);
                return outerProvider().get(transformedKey);
            }
        };
        return result;
    }

    /**
     * Return a resource provider, the resource from which will transparently
     * retry calls to {@link WritableResource#write(Object)} that throw, as
     * with {@link Observable#retry(int)}. Specifically, any
     * {@link Observable} returned by {@link WritableResource#write(Object)}
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
    public FluentWritableResourceProvider<KEY, RSRC, RESPONSE> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new FluentWritableResourceProvider<KEY, RSRC, RESPONSE>() {
                @Override
                public FluentWritableResource<RSRC, RESPONSE> get(final KEY key) {
                    return outerProvider()
                            .get(key)
                            .retry(maxRetries);
                }
            };
        }
    }

    public <TO> FluentWritableResourceProvider<KEY, RSRC, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Preconditions.checkNotNull(bind, "null operator");
        return new FluentWritableResourceProvider<KEY, RSRC, TO>() {
            @Override
            public FluentWritableResource<RSRC, TO> get(final KEY key) {
                final FluentWritableResource<RSRC, TO> resource = outerProvider()
                        .get(key)
                        .lift(bind);
                return resource;
            }
        };
    }

    public Func1<KEY, FluentWritableResource<RSRC, RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function<KEY, FluentWritableResource<RSRC, RESPONSE>>(this) {
            @Override
            public FluentWritableResource<RSRC, RESPONSE> call(final KEY key) {
                return get(key);
            }
        };
    }

    @Override
    public abstract FluentWritableResource<RSRC, RESPONSE> get(KEY key);

    private FluentWritableResourceProvider<KEY, RSRC, RESPONSE> outerProvider() {
        return this;
    }

}
