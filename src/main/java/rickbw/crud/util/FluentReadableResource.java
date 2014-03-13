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
import rx.Observable;
import rx.functions.Func1;


/**
 * A set of fluent transformations on {@link ReadableResource}s.
 */
public abstract class FluentReadableResource<RSRC> implements ReadableResource<RSRC> {

    public static <RSRC> FluentReadableResource<RSRC> from(final ReadableResource<RSRC> resource) {
        if (resource instanceof FluentReadableResource<?>) {
            return (FluentReadableResource<RSRC>) resource;
        } else {
            Preconditions.checkNotNull(resource);
            return new FluentReadableResource<RSRC>() {
                @Override
                public Observable<RSRC> get() {
                    return resource.get();
                }

                // TODO: override equals() and hashCode()
            };
        }
    }

    public <TO> FluentReadableResource<TO> mapValue(final Func1<? super RSRC, ? extends TO> mapper) {
        Preconditions.checkNotNull(mapper, "null function");
        return new FluentReadableResource<TO>() {
            @Override
            public Observable<TO> get() {
                final Observable<? extends RSRC> observable = outerResource().get();
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }

            // TODO: override equals() and hashCode()
        };
    }

    // TODO: Expose other Observable methods

    // TODO: Adapt Subscriber

    private FluentReadableResource<RSRC> outerResource() {
        return this;
    }

}