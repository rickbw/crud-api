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
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link DeletableResource}s.
 */
public abstract class FluentDeletableResource<RESPONSE> implements DeletableResource<RESPONSE> {

    public static <RESPONSE> FluentDeletableResource<RESPONSE> from(final DeletableResource<RESPONSE> resource) {
        if (resource instanceof FluentDeletableResource<?>) {
            return (FluentDeletableResource<RESPONSE>) resource;
        } else {
            Preconditions.checkNotNull(resource);
            return new FluentDeletableResource<RESPONSE>() {
                @Override
                public Observable<RESPONSE> delete() {
                    return resource.delete();
                }
            };
        }
    }

    /**
     * Access the response to deleting the given resource as a resource in its
     * own right.
     */
    public FluentReadableResource<RESPONSE> asReadableResource() {
        final FluentReadableResource<RESPONSE> wrapper = new FluentReadableResource<RESPONSE>() {
            @Override
            public Observable<RESPONSE> get() {
                // FIXME: Memoize the result so we don't delete over and over!
                final Observable<RESPONSE> response = delete();
                return response;
            }
        };
        return wrapper;
    }

    public <TO> FluentDeletableResource<TO> mapResponse(final Func1<? super RESPONSE, ? extends TO> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        final FluentDeletableResource<TO> result = new FluentDeletableResource<TO>() {
            @Override
            public Observable<TO> delete() {
                final Observable<? extends RESPONSE> observable = outerResource().delete();
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
        final DeletableResource<TO> newDelegate = result;
        return from(newDelegate);
    }

    // TODO: Expose other Observable methods

    // TODO: Adapt Subscriber

    private FluentDeletableResource<RESPONSE> outerResource() {
        return this;
    }

}
