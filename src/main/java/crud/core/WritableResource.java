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

import javax.annotation.Nonnull;

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
 *
 * @author Rick Warren
 */
public interface WritableResource<RSRC, RESPONSE> extends Resource {

    /**
     * Replace or update the value of the resource with the given value. It is
     * unspecified whether this is a full or partial update, or whether the
     * result will differ depending on whether the resource already has a
     * value; those variants are implementation-defined.
     * <p/>
     * Return to the application a stream of zero or more values indicating
     * the result of the write operation. That result may indicate
     * success/failure, number of records modified, or any other relevant
     * meta-data specific to the {@link WritableResource} implementation. The
     * preferred return type for implementations that need only return a
     * success/failure result is {@code Observable<Void>}.
     * <p/>
     * The write begins immediately when this method is called. Callers need
     * not {@link Observable#subscribe(rx.Observer) subscribe} to the result
     * in order for the write to occur, and in fact may safely ignore they
     * don't care what it is.
     * <p/>
     * <b>Note to implementers</b>: Except in special cases, writing to this
     * {@link WritableResource} will involve I/O. In the interest of segregating those
     * threads that perform blocking I/O and those that perform computation,
     * implementations should not block on this call, nor should the result(s)
     * be delivered in the thread {@link Observable#subscribe() subscribing}
     * to the Observable. Instead, writes should occur in a thread associated
     * with the {@link Session} used to create this {@code ReadableResource}.
     * <p/>
     * <b>Design Rationale</b>: Why does this method take a single value,
     * rather than an {@link Observable} of (perhaps multiple) value(s)?
     * Two reasons:
     * <ol>
     *  <li>It is not possible to write a value one does not yet have, and
     *      given this constraint, it is desirable that the method signature
     *      emphasize that writing takes place immediately with the call, not
     *      at some future time.</li>
     *  <li>To the extent that an application <em>does</em> want to write a
     *      value upon the availability of elements emitted by an
     *      {@link Observable}, that availability is already encapsulated by
     *      that Observable. Rather than represent deferred action at both
     *      source and sink, it is simpler to represent it only at the former,
     *      to allow straightforward constructions like
     *      {@link Observable#forEach(rx.functions.Action1) myObservable.forEach(write)}.</li>
     * </ol>
     *
     * @throws IllegalArgumentException If the new value is malformed in some
     *         way that is detectable at invocation time.
     *
     * @see Observable#subscribe(rx.Observer)
     */
    public abstract Observable<RESPONSE> write(@Nonnull RSRC newValue);

}
