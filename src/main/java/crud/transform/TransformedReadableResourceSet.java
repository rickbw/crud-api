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
package crud.transform;

import java.util.Objects;

import crud.core.ReadableResourceSet;
import crud.core.Session;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;


public abstract class TransformedReadableResourceSet<KEY, RSRC>
implements ReadableResourceSet<KEY, RSRC> {

    public static <KEY, RSRC> TransformedReadableResourceSet<KEY, RSRC> from(
            final ReadableResourceSet<KEY, RSRC> rsrcSet) {
        if (rsrcSet instanceof TransformedReadableResourceSet<?, ?>) {
            return (TransformedReadableResourceSet<KEY, RSRC>) rsrcSet;
        } else {
            return new TransformedReadableResourceSet<KEY, RSRC>() {
                @Override
                public TransformedReadableResource<RSRC> get(final KEY key, final Session session) {
                    return TransformedReadableResource.from(rsrcSet.get(key, session));
                }
            };
        }
    }

    public <R> TransformedReadableResourceSet<KEY, R> mapValue(
            final Func1<? super Observable<? super RSRC>, ? extends Observable<R>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final TransformedReadableResourceSet<KEY, R> result = new TransformedReadableResourceSet<KEY, R>() {
            @Override
            public TransformedReadableResource<R> get(final KEY key, final Session session) {
                return outerResourceSet()
                        .get(key, session)
                        .mapValue(mapper);
            }
        };
        return result;
    }

    public <K> TransformedReadableResourceSet<K, RSRC> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final TransformedReadableResourceSet<K, RSRC> result = new TransformedReadableResourceSet<K, RSRC>() {
            @Override
            public TransformedReadableResource<RSRC> get(final K key, final Session session) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerResourceSet().get(transformedKey, session);
            }
        };
        return result;
    }

    public Func2<KEY, Session, TransformedReadableResource<RSRC>> toFunction() {
        return new Func2<KEY, Session, TransformedReadableResource<RSRC>>() {
            @Override
            public TransformedReadableResource<RSRC> call(final KEY key, final Session session) {
                return get(key, session);
            }
        };
    }

    @Override
    public abstract TransformedReadableResource<RSRC> get(KEY key, Session session);

    private TransformedReadableResourceSet<KEY, RSRC> outerResourceSet() {
        return this;
    }

}
