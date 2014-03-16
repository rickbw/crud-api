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

package rickbw.crud.util;

import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class FluentWritableResourceProvider<KEY, RSRC, RESPONSE>
implements WritableResourceProvider<KEY, RSRC, RESPONSE> {

    public static <KEY, RSRC, RESPONSE> FluentWritableResourceProvider<KEY, RSRC, RESPONSE> from(
            final WritableResourceProvider<? super KEY, ? super RSRC, ? extends RESPONSE> provider) {
        /* XXX: The casts below should be safe, since the API only consumes
         * instances of KEY and RSRC and only produces instances of RESPONSE.
         * However, the Java generic wildcards don't want to cooperate.
         */
        if (provider instanceof FluentWritableResourceProvider<?, ?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentWritableResourceProvider<KEY, RSRC, RESPONSE> typedProvider
                    = (FluentWritableResourceProvider<KEY, RSRC, RESPONSE>) provider;
            return typedProvider;
        } else {
            return new FluentWritableResourceProvider<KEY, RSRC, RESPONSE>() {
                @Override
                public FluentWritableResource<RSRC, RESPONSE> get(final KEY key) {
                    final WritableResource<RSRC, RESPONSE> rsrc
                            = (WritableResource<RSRC, RESPONSE>) provider.get(key);
                    return FluentWritableResource.from(rsrc);
                }
            };
        }
    }

    public <RESP> FluentWritableResourceProvider<KEY, RSRC, RESP> map(
            final Func1<? super RESPONSE, ? extends RESP> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentWritableResourceProvider<KEY, RSRC, RESP> result = new FluentWritableResourceProvider<KEY, RSRC, RESP>() {
            @Override
            public FluentWritableResource<RSRC, RESP> get(final KEY key) {
                final WritableResource<? super RSRC, ? extends RESPONSE> resource = outerProvider().get(key);
                final WritableResource<RSRC, RESP> mapped = FluentWritableResource.from(resource).mapResponse(mapper);
                return FluentWritableResource.from(mapped);
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
                final WritableResource<? super RSRC, RESPONSE> resource = outerProvider().get(key);
                final WritableResource<RC, RESPONSE> transformed = FluentWritableResource.from(resource).adaptNewValue(adapter);
                return FluentWritableResource.from(transformed);
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
                final WritableResource<RSRC, RESPONSE> resource = outerProvider().get(transformedKey);
                return FluentWritableResource.from(resource);
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
                    final FluentWritableResource<RSRC, RESPONSE> resource = outerProvider().get(key)
                            .retry(maxRetries);
                    return resource;
                }
            };
        }
    }

    public <TO> FluentWritableResourceProvider<KEY, RSRC, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Preconditions.checkNotNull(bind, "null operator");
        return new FluentWritableResourceProvider<KEY, RSRC, TO>() {
            @Override
            public FluentWritableResource<RSRC, TO> get(final KEY key) {
                final FluentWritableResource<RSRC, TO> resource = outerProvider().get(key)
                        .lift(bind);
                return resource;
            }
        };
    }

    @Override
    public abstract FluentWritableResource<RSRC, RESPONSE> get(KEY key);

    private FluentWritableResourceProvider<KEY, RSRC, RESPONSE> outerProvider() {
        return this;
    }

}
