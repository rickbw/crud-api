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

import crud.core.Session;
import crud.core.WritableResourceSet;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;


public abstract class TransformedWritableResourceSet<KEY, RSRC, RESPONSE>
implements WritableResourceSet<KEY, RSRC, RESPONSE> {

    public static <KEY, RSRC, RESPONSE> TransformedWritableResourceSet<KEY, RSRC, RESPONSE> from(
            final WritableResourceSet<KEY, RSRC, RESPONSE> rsrcSet) {
        if (rsrcSet instanceof TransformedWritableResourceSet<?, ?, ?>) {
            return (TransformedWritableResourceSet<KEY, RSRC, RESPONSE>) rsrcSet;
        } else {
            return new TransformedWritableResourceSet<KEY, RSRC, RESPONSE>() {
                @Override
                public TransformedWritableResource<RSRC, RESPONSE> get(final KEY key, final Session session) {
                    return TransformedWritableResource.from(rsrcSet.get(key, session));
                }
            };
        }
    }

    public <RESP> TransformedWritableResourceSet<KEY, RSRC, RESP> mapResponse(
            final Func1<? super Observable<RESPONSE>, ? extends Observable<RESP>> mapper) {
        Objects.requireNonNull(mapper, "null function");
        final TransformedWritableResourceSet<KEY, RSRC, RESP> result = new TransformedWritableResourceSet<KEY, RSRC, RESP>() {
            @Override
            public TransformedWritableResource<RSRC, RESP> get(final KEY key, final Session session) {
                return outerResourceSet()
                        .get(key, session)
                        .mapResponse(mapper);
            }
        };
        return result;
    }

    public <RC> TransformedWritableResourceSet<KEY, RC, RESPONSE> adaptNewValue(
            final Func1<? super RC, ? extends RSRC> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final TransformedWritableResourceSet<KEY, RC, RESPONSE> result = new TransformedWritableResourceSet<KEY, RC, RESPONSE>() {
            @Override
            public TransformedWritableResource<RC, RESPONSE> get(final KEY key, final Session session) {
                return outerResourceSet()
                        .get(key, session)
                        .<RC>adaptNewValue(adapter);
            }
        };
        return result;
    }

    public <K> TransformedWritableResourceSet<K, RSRC, RESPONSE> adaptKey(
            final Func1<? super K, ? extends KEY> adapter) {
        Objects.requireNonNull(adapter, "null function");
        final TransformedWritableResourceSet<K, RSRC, RESPONSE> result = new TransformedWritableResourceSet<K, RSRC, RESPONSE>() {
            @Override
            public TransformedWritableResource<RSRC, RESPONSE> get(final K key, final Session session) {
                Objects.requireNonNull(key, "null key");
                final KEY transformedKey = adapter.call(key);
                return outerResourceSet().get(transformedKey, session);
            }
        };
        return result;
    }

    public Func2<KEY, Session, TransformedWritableResource<RSRC, RESPONSE>> toFunction() {
        return new DelegateObjectMethods.Function2<KEY, Session, TransformedWritableResource<RSRC, RESPONSE>>(this) {
            @Override
            public TransformedWritableResource<RSRC, RESPONSE> call(final KEY key, final Session session) {
                return get(key, session);
            }
        };
    }

    @Override
    public abstract TransformedWritableResource<RSRC, RESPONSE> get(KEY key, Session session);

    private TransformedWritableResourceSet<KEY, RSRC, RESPONSE> outerResourceSet() {
        return this;
    }

}
