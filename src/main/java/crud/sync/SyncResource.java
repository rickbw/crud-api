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

import crud.core.Resource;
import crud.implementer.AsyncResults;


public abstract class SyncResource<RSRC, D extends Resource<RSRC>>
extends SyncDelegateHolder<D>
implements AutoCloseable {

    /**
     * @see Resource#shutdown()
     */
    @Override
    public final void close() throws Exception {
        AsyncResults.awaitShutdown(getDelegate());
    }

    /*package*/ SyncResource(final D delegate) {
        super(delegate);
    }

}
