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
import rickbw.crud.UpdatableResource;
import rx.Observable;
import rx.util.functions.Func1;


/**
 * A set of fluent transformations on {@link UpdatableResource}s.
 *
 * You may notice that there is a method
 * {@link FluentWritableResource#asReadableResource(Object)}
 * and a method
 * {@link FluentDeletableResource#asReadableResource()};
 * however, there is no <code>UpdatableResources.asReadableResource<code>.
 * This is by design: Read, Write, and Delete are all idempotent operations;
 * Update is not. To expose an Update as a Read would be to violate the
 * contract of {@link ReadableResource}.
 */
public abstract class FluentUpdatableResource<UPDATE, RESPONSE> implements UpdatableResource<UPDATE, RESPONSE> {

    public static <UPDATE, RESPONSE> FluentUpdatableResource<UPDATE, RESPONSE> from(
            final UpdatableResource<? super UPDATE, RESPONSE> resource) {
        if (resource instanceof FluentUpdatableResource<?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentUpdatableResource<UPDATE, RESPONSE> result = (FluentUpdatableResource<UPDATE, RESPONSE>) resource;
            return result;
        } else {
            Preconditions.checkNotNull(resource);
            return new FluentUpdatableResource<UPDATE, RESPONSE>() {
                @Override
                public Observable<RESPONSE> update(final UPDATE update) {
                    return resource.update(update);
                }
            };
        }
    }

    public <TO> FluentUpdatableResource<UPDATE, TO> mapResponse(
            final Func1<? super RESPONSE, ? extends TO> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        return new FluentUpdatableResource<UPDATE, TO>() {
            @Override
            public Observable<TO> update(final UPDATE update) {
                final Observable<? extends RESPONSE> observable = outerResource().update(update);
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
    }

    // TODO: Expose other Observable methods

    public <TO> FluentUpdatableResource<TO, RESPONSE> adaptUpdate(
            final Func1<? super TO, ? extends UPDATE> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        return new FluentUpdatableResource<TO, RESPONSE>() {
            @Override
            public Observable<RESPONSE> update(final TO update) {
                final UPDATE transformed = adapter.call(update);
                final Observable<RESPONSE> observable = outerResource().update(transformed);
                return observable;
            }
        };
    }

    // TODO: Transform Subscriber

    private FluentUpdatableResource<UPDATE, RESPONSE> outerResource() {
        return this;
    }

}
