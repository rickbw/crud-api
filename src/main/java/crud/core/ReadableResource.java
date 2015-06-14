/* Copyright 2013–2015 Rick Warren
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
import rx.Observer;


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
 * @see ReadableResourceSet
 *
 * @author Rick Warren
 */
public interface ReadableResource<RSRC> extends Resource {

    /**
     * Request the value(s) of this resource. Zero or more values will be
     * delivered when they are ready. The {@link Observable} may be
     * either "hot" or "cold", depending on the nature of the resource.
     * <p/>
     * This operation is idempotent.
     * <p/>
     * <b>Note to implementers</b>: Except in special cases, consuming from
     * this {@link ReadableResource} will involve I/O. In the interest of
     * segregating those threads that perform blocking I/O and those that
     * perform computation, implementations should not block on this call, nor
     * should elements be delivered in the thread
     * {@link Observable#subscribe() subscribing} to the Observable. Instead,
     * data elements should be delivered to {@link Observer}s in a thread
     * associated with the {@link Session} used to create this
     * {@code ReadableResource}.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RSRC> read();

}
