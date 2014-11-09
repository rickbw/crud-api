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
package crud.spi;

import rx.Observable;


/**
 * Allows a client to replace the value of a resource. Any resource that
 * supports setting a new value should implement this interface.
 *
 * @param <RSRC>        The type of this resource's value.
 * @param <RESPONSE>    The type of the response(s) that may be returned as
 *        a result of the change in the value.
 *
 * @see GettableSpec
 * @see UpdatableSpec
 * @see DeletableSpec
 * @see SettableProviderSpec
 */
public interface SettableSpec<RSRC, RESPONSE> extends Resource {

    /**
     * Replace the value of the resource with the value(s) emitted by the
     * given {@link Observable}. If the resource
     * previously had no value, it will be initialized with the provided
     * value(s). The operation may return one or more responses via the
     * given {@link Observable} as the request is acted upon.
     *
     * This operation is idempotent.
     *
     * @throws NullPointerException     If the given value is null. In
     *         particular, writing null is <em>not</em> equivalent to
     *         deletion; see {@link DeletableSpec}.
     * @throws IllegalArgumentException If the new value is malformed in some
     *         way that is detectable at invocation time.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    Observable<RESPONSE> set(Observable<RSRC> newValue);

}
