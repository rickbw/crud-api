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
 * Allows a client to perform a partial update of the value of a resource. Any
 * resource that supports partial updates should implement this interface.
 *
 * @param <UPDATE>      The type of the update to be applied to the value of
 *        the resource. It may or may not be the same as the type of the
 *        resource itself.
 * @param <RESPONSE>    The type of the response(s) that may come back as a
 *        result of the update. It may or may not be the same as the type of
 *        the resource itself.
 *
 * @see ReadableSpec
 * @see WritableSpec
 * @see DeletableSpec
 * @see UpdatableProviderSpec
 */
public interface UpdatableSpec<UPDATE, RESPONSE> extends Resource {

    /**
     * Update the value of the resource in an unspecified way based on the
     * given value. The operation may return one or more responses via the
     * given {@link Observable} as the request is acted upon.
     *
     * This operation is <em>not</em> idempotent, in general.
     *
     * @throws NullPointerException     If the given update is null.
     * @throws IllegalArgumentException If the update is otherwise malformed
     *         in some way that is detectable at invocation time.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RESPONSE> update(UPDATE update);

}
