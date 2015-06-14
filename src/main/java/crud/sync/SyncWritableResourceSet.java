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

import crud.core.Session;
import crud.core.WritableResource;
import crud.core.WritableResourceSet;


/**
 * @see WritableResourceSet
 *
 * @author Rick Warren
 */
public class SyncWritableResourceSet<KEY, RSRC, RESPONSE>
extends SyncResourceSet<KEY, RSRC, WritableResourceSet<KEY, RSRC, RESPONSE>> {

    public SyncWritableResourceSet(@Nonnull final WritableResourceSet<KEY, RSRC, RESPONSE> delegate) {
        super(delegate);
    }

    /**
     * @see WritableResourceSet#get(Object, Session)
     */
    @Override
    public @Nonnull SyncWritableResource<RSRC, RESPONSE> get(@Nonnull final KEY key, @Nonnull final SyncSession session) {
        final WritableResource<RSRC, RESPONSE> delegate = getDelegate().get(key, session.getDelegate());
        return new SyncWritableResource<>(delegate);
    }

}
