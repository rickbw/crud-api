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

package rickbw.crud.pattern;

import java.util.Map;

import rickbw.crud.DeletableResource;
import rickbw.crud.DeletableResourceProvider;
import rickbw.crud.ReadableResource;
import rickbw.crud.ReadableResourceProvider;
import rickbw.crud.Resource;
import rickbw.crud.WritableResource;
import rickbw.crud.WritableResourceProvider;


/**
 * A combination of {@link Resource} operations likely to occur when using a
 * key-value data source, such as a {@link Map} or a Voldemort store. A
 * {@link KeyValueResource.Provider} is analogous to an asynchronous Map; a
 * resource itself is analogous to a single {@link java.util.Map.Entry}.
 */
public interface KeyValueResource<RSRC, WR_RESP, DEL_RESP>
extends ReadableResource<RSRC>,
        WritableResource<RSRC, WR_RESP>,
        DeletableResource<DEL_RESP> {

    public static interface Provider<KEY, RSRC, WR_RESP, DEL_RESP>
    extends ReadableResourceProvider<KEY, RSRC>,
            WritableResourceProvider<KEY, RSRC, WR_RESP>,
            DeletableResourceProvider<KEY, DEL_RESP> {
        @Override
        public abstract KeyValueResource<RSRC, WR_RESP, DEL_RESP> get(KEY key);
    }

}
