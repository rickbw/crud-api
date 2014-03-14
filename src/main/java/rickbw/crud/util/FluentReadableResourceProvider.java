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

import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class FluentReadableResourceProvider<KEY, RSRC>
implements ReadableResourceProvider<KEY, RSRC> {

    public static <KEY, RSRC> FluentReadableResourceProvider<KEY, RSRC> from(
            final ReadableResourceProvider<? super KEY, ? extends RSRC> provider) {
        /* XXX: The casts below should be safe, since the API only consumes
         * instances of KEY and only produces instances of RSRC.
         * However, the Java generic wildcards don't want to cooperate.
         */
        if (provider instanceof FluentReadableResourceProvider<?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentReadableResourceProvider<KEY, RSRC> typedProvider
                    = (FluentReadableResourceProvider<KEY, RSRC>) provider;
            return typedProvider;
        } else {
            return new FluentReadableResourceProvider<KEY, RSRC>() {
                @Override
                public FluentReadableResource<RSRC> get(final KEY key) {
                    final ReadableResource<RSRC> resource
                            = (ReadableResource<RSRC>) provider.get(key);
                    return FluentReadableResource.from(resource);
                }
            };
        }
    }

    public <R> FluentReadableResourceProvider<KEY, R> map(
            final Func1<? super RSRC, ? extends R> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentReadableResourceProvider<KEY, R> result = new FluentReadableResourceProvider<KEY, R>() {
            @Override
            public FluentReadableResource<R> get(final KEY key) {
                final ReadableResource<? extends RSRC> resource = outerProvider().get(key);
                final ReadableResource<R> mapped = FluentReadableResource.from(resource).mapValue(mapper);
                return FluentReadableResource.from(mapped);
            }
        };
        return result;
    }

    public <K> FluentReadableResourceProvider<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentReadableResourceProvider<K, RSRC> result = new FluentReadableResourceProvider<K, RSRC>() {
            @Override
            public FluentReadableResource<RSRC> get(final K key) {
                final KEY transformedKey = adapter.call(key);
                final ReadableResource<RSRC> resource = outerProvider().get(transformedKey);
                return FluentReadableResource.from(resource);
            }
        };
        return result;
    }

    /**
     * Return a resource provider, the resource from which will transparently
     * retry calls to {@link ReadableResource#get()} that throw, as with
     * {@link Observable#retry(int)}. Specifically, any {@link Observable}
     * returned by {@link ReadableResource#get()} will re-subscribe up to
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
    public FluentReadableResourceProvider<KEY, RSRC> retry(final int maxRetries) {
        if (maxRetries == 0) {
            return this;    // no-op
        } else if (maxRetries < 0) {
            throw new IllegalArgumentException("maxRetries " + maxRetries + " < 0");
        } else {
            return new FluentReadableResourceProvider<KEY, RSRC>() {
                @Override
                public FluentReadableResource<RSRC> get(final KEY key) {
                    final FluentReadableResource<RSRC> resource = outerProvider().get(key)
                            .retry(maxRetries);
                    return resource;
                }
            };
        }
    }

    public <TO> FluentReadableResourceProvider<KEY, TO> lift(final Observable.Operator<TO, RSRC> bind) {
        Preconditions.checkNotNull(bind, "null operator");
        return new FluentReadableResourceProvider<KEY, TO>() {
            @Override
            public FluentReadableResource<TO> get(final KEY key) {
                final FluentReadableResource<TO> resource = outerProvider().get(key)
                        .lift(bind);
                return resource;
            }
        };
    }

    @Override
    public abstract FluentReadableResource<RSRC> get(KEY key);

    private FluentReadableResourceProvider<KEY, RSRC> outerProvider() {
        return this;
    }

}
