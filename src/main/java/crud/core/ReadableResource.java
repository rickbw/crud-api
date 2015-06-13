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
package crud.core;

import rx.Observable;


/**
 * Allows a client to read the value(s) of a resource. Any resource whose
 * value can be read should implement this interface.
 *
 * Reads must be idempotent, and they must not modify the state of the
 * resource they read.
 *
 * @param <RSRC>    The type of the resource's value(s).
 *
 * @see WritableResource
 * @see UpdatableResource
 * @see DeletableResource
 * @see ReadableResourceProvider
 */
public interface ReadableResource<RSRC> extends Resource {

    /**
     * Request the value(s) of this resource. Zero or more values will be
     * delivered when they are ready.
     *
     * This operation is idempotent.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RSRC> read();

}
