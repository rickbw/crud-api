/* Copyright 2015 Rick Warren
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
package crud.sync;

import javax.annotation.Nonnull;

import crud.core.WritableResource;
import crud.core.Session;
import crud.core.WritableResourceSet;
import crud.core.WritableResourceSet.Id;


/**
 * @see WritableResourceSet
 *
 * @author Rick Warren
 */
public class SyncWritableResourceSet<K, E, R> extends SyncDelegateHolder<WritableResourceSet<K, E, R>> {

    public SyncWritableResourceSet(@Nonnull final WritableResourceSet<K, E, R> delegate) {
        super(delegate);
    }

    /**
     * @see WritableResourceSet#getId()
     */
    public @Nonnull Id<K, E, R> getId() {
        return getDelegate().getId();
    }

    /**
     * @see WritableResourceSet#resource(Object, Session)
     */
    public @Nonnull SyncWritableResource<E, R> resource(@Nonnull final K key, @Nonnull final SyncSession session) {
        final WritableResource<E, R> delegate = getDelegate().resource(key, session.getDelegate());
        return new SyncWritableResource<>(delegate);
    }

}
