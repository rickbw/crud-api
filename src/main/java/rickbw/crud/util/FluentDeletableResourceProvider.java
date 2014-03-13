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

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rx.Observable;
import rx.Observer;
import rx.functions.Func1;


public abstract class FluentDeletableResourceProvider<KEY, RESPONSE>
implements DeletableResourceProvider<KEY, RESPONSE> {

    public static <KEY, RESPONSE> FluentDeletableResourceProvider<KEY, RESPONSE> from(
            final DeletableResourceProvider<? super KEY, ? extends RESPONSE> provider) {
        /* XXX: The casts below should be safe, since the API only consumes
         * instances of KEY and only produces instances of RESPONSE.
         * However, the Java generic wildcards don't want to cooperate.
         */
        if (provider instanceof FluentDeletableResourceProvider<?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentDeletableResourceProvider<KEY, RESPONSE> typedProvider
                    = (FluentDeletableResourceProvider<KEY, RESPONSE>) provider;
            return typedProvider;
        } else {
            return new FluentDeletableResourceProvider<KEY, RESPONSE>() {
                @Override
                public FluentDeletableResource<RESPONSE> get(final KEY key) {
                    final DeletableResource<RESPONSE> resource
                            = (DeletableResource<RESPONSE>) provider.get(key);
                    return FluentDeletableResource.from(resource);
                }
            };
        }
    }

    public <R> FluentDeletableResourceProvider<KEY, R> map(
            final Func1<? super RESPONSE, ? extends R> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentDeletableResourceProvider<KEY, R> result = new FluentDeletableResourceProvider<KEY, R>() {
            @Override
            public FluentDeletableResource<R> get(final KEY key) {
                final DeletableResource<? extends RESPONSE> resource = outerProvider().get(key);
                final DeletableResource<R> mapped = FluentDeletableResource.from(resource).mapResponse(mapper);
                return FluentDeletableResource.from(mapped);
            }
        };
        return result;
    }

    public <K> FluentDeletableResourceProvider<K, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        final FluentDeletableResourceProvider<K, RESPONSE> result = new FluentDeletableResourceProvider<K, RESPONSE>() {
            @Override
            public FluentDeletableResource<RESPONSE> get(final K key) {
                final KEY transformedKey = adapter.call(key);
                final DeletableResource<RESPONSE> resource = outerProvider().get(transformedKey);
                return FluentDeletableResource.from(resource);
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
                    final FluentDeletableResource<RESPONSE> resource = outerProvider().get(key)
                            .retry(maxRetries);
                    return resource;
                }
            };
        }
    }

    public <TO> FluentDeletableResourceProvider<KEY, TO> lift(final Observable.Operator<TO, RESPONSE> bind) {
        Preconditions.checkNotNull(bind, "null operator");
        return new FluentDeletableResourceProvider<KEY, TO>() {
            @Override
            public FluentDeletableResource<TO> get(final KEY key) {
                final FluentDeletableResource<TO> resource = outerProvider().get(key)
                        .lift(bind);
                return resource;
            }
        };
    }

    @Override
    public abstract FluentDeletableResource<RESPONSE> get(KEY key);

    private FluentDeletableResourceProvider<KEY, RESPONSE> outerProvider() {
        return this;
    }

}
