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


/**
 * Allows a client to replace or update the value of a resource.
 *
 * @param <RSRC>        The type of this resource's value.
 * @param <RESPONSE>    The type of the response(s) that may be returned as
 *        a result of the change in the value.
 *
 * @see ReadableResource
 * @see WritableResourceSet
 */
public interface WritableResource<RSRC, RESPONSE> extends Resource {

    /**
     * Replace or update the value of the resource with the given value. It is
     * unspecified whether this is a full or partial update, or whether the
     * result will differ depending on whether the resource already has a
     * value; those variants are implementation-defined. The operation may
     * return one or more responses via the returned {@link Observable} as the
     * request is acted upon.
     *
     * @throws NullPointerException     If the given value is null.
     * @throws IllegalArgumentException If the new value is malformed in some
     *         way that is detectable at invocation time.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RESPONSE> write(RSRC newValue);

}
