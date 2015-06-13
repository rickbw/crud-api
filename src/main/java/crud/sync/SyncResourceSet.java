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

import crud.core.ResourceSet;
import crud.core.Session;


/**
 * @see ResourceSet
 */
public abstract class SyncResourceSet<K, E, D extends ResourceSet<K, E>>
extends SyncDelegateHolder<D> {

    /**
     * @see ResourceSet#getId()
     */
    public abstract @Nonnull ResourceSet.Id<K, E> getId();

    /**
     * @see ResourceSet#resource(Object, Session)
     */
    public abstract @Nonnull SyncResource<?> resource(
            @Nonnull final K key,
            @Nonnull final SyncSession session);

    /*package*/ SyncResourceSet(final D delegate) {
        super(delegate);
    }

}
