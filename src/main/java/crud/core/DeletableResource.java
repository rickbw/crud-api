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
 * Allows a client to delete the value from a resource. Any resource whose
 * value can be deleted should implement this interface.
 *
 * @param <RESPONSE>    The type of the deletion response(s), if any.
 *
 * @see ReadableResource
 * @see WritableResource
 * @see UpdatableResource
 * @see DeletableResourceProvider
 */
public interface DeletableResource<RESPONSE> extends Resource {

    /**
     * Issue a (possibly asynchronous) request to delete the value of this
     * resource. A response will be delivered when it is ready. The response
     * may or may not be of the same type as the resource itself, depending
     * on the implementation.
     *
     * This operation is idempotent.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RESPONSE> delete();

}
