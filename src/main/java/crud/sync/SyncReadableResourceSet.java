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
public class SyncReadableResourceSet<KEY, RSRC>
extends SyncResourceSet<KEY, RSRC, ReadableResourceSet<KEY, RSRC>> {

    public SyncReadableResourceSet(@Nonnull final ReadableResourceSet<KEY, RSRC> delegate) {
        super(delegate);
    }

    /**
     * @see ReadableResourceSet#get(Object, Session)
     */
    @Override
    public @Nonnull SyncReadableResource<RSRC> get(
            @Nonnull final KEY key,
            @Nonnull final SyncSession session) {
        final ReadableResource<RSRC> delegateSource = getDelegate().get(key, session.getDelegate());
        return new SyncReadableResource<>(delegateSource);
    }

}
