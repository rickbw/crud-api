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

import crud.core.ReadableResource;
import crud.core.ReadableResourceSet;
import crud.core.Session;


/**
 * @see ReadableResourceSet
 *
 * @author Rick Warren
 */
public class SyncReadableResourceSet<K, E> extends SyncResourceSet<K, E, ReadableResourceSet<K, E>> {

    public SyncReadableResourceSet(@Nonnull final ReadableResourceSet<K, E> delegate) {
        super(delegate);
    }

    /**
     * @see ReadableResourceSet#getId()
     */
    @Override
    public @Nonnull ReadableResourceSet.Id<K, E> getId() {
        return getDelegate().getId();
    }

    /**
     * @see ReadableResourceSet#get(Object, Session)
     */
    @Override
    public @Nonnull SyncReadableResource<E> get(@Nonnull final K key, @Nonnull final SyncSession session) {
        final ReadableResource<E> delegateSource = getDelegate().get(key, session.getDelegate());
        return new SyncReadableResource<>(delegateSource);
    }

}