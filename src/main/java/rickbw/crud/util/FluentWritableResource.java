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
import rx.Observable;
import rx.util.functions.Func1;


/**
 * A set of fluent transformations on {@link WritableResource}s.
 */
public abstract class FluentWritableResource<RSRC, RESPONSE> implements WritableResource<RSRC, RESPONSE> {

    public static <RSRC, RESPONSE> FluentWritableResource<RSRC, RESPONSE> from(
            final WritableResource<? super RSRC, RESPONSE> resource) {
        if (resource instanceof FluentWritableResource<?, ?>) {
            @SuppressWarnings("unchecked")
            final FluentWritableResource<RSRC, RESPONSE> result = (FluentWritableResource<RSRC, RESPONSE>) resource;
            return result;
        } else {
            Preconditions.checkNotNull(resource);
            return new FluentWritableResource<RSRC, RESPONSE>() {
                @Override
                public Observable<RESPONSE> write(final RSRC newValue) {
                    return resource.write(newValue);
                }
            };
        }
    }

    /**
     * Access the response to writing the given resource as a resource in its
     * own right.
     */
    public FluentReadableResource<RESPONSE> asReadableResource(final RSRC newValue) {
        final FluentReadableResource<RESPONSE> wrapper = new FluentReadableResource<RESPONSE>() {
            @Override
            public Observable<RESPONSE> get() {
                // FIXME: Memoize result so we don't write over and over!
                final Observable<RESPONSE> response = write(newValue);
                return response;
            }
        };
        return wrapper;
    }

    public <TO> FluentWritableResource<RSRC, TO> mapResponse(final Func1<? super RESPONSE, ? extends TO> mapper) {
        Preconditions.checkNotNull(mapper, "null function");

        return new FluentWritableResource<RSRC, TO>() {
            @Override
            public Observable<TO> write(final RSRC value) {
                final Observable<? extends RESPONSE> observable = outerResource().write(value);
                final Observable<TO> mapped = observable.map(mapper);
                return mapped;
            }
        };
    }

    // TODO: Expose other Observable methods

    public <TO> FluentWritableResource<TO, RESPONSE> adaptNewValue(
            final Func1<? super TO, ? extends RSRC> adapter) {
        Preconditions.checkNotNull(adapter, "null function");

        return new FluentWritableResource<TO, RESPONSE>() {
            @Override
            public Observable<RESPONSE> write(final TO value) {
                final RSRC transformed = adapter.call(value);
                final Observable<RESPONSE> observable = outerResource().write(transformed);
                return observable;
            }
        };
    }

    // TODO: Adapt Subscriber

    private FluentWritableResource<RSRC, RESPONSE> outerResource() {
        return this;
    }

}
