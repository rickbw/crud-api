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


/**
 * @see ReadableResource
 *
 * @author Rick Warren
 */
public class SyncReadableResource<RSRC> extends SyncResource<RSRC, ReadableResource<RSRC>> {

    public SyncReadableResource(@Nonnull final ReadableResource<RSRC> delegate) {
        super(delegate);
    }

    /**
     * @see ReadableResource#read()
     */
    public Iterable<RSRC> read() {
        return getDelegate().read().toBlocking().toIterable();
    }

}
