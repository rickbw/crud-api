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
package crud.core;

import rx.Observable;


/**
 * A <em>sink</em> for data elements of type {@code E}, written by the
 * application to somewhere else.
 *
 * @param <E>   The static type of the data elements written to this
 *              {@link WritableResource}.
 * @param <R>   The static type of the result of writing a value.
 *
 * @see ReadableResource
 *
 * @author Rick Warren
 */
public interface WritableResource<E, R> extends Resource {

    /**
     * Write a single data element to the target middleware. Return to the
     * application a stream of zero or more values indicating the result of
     * the write operation. That result may indicate success/failure, number
     * of records modified, or any other relevant meta-data specific to the
     * {@link WritableResource} implementation. The preferred return type for
     * implementations that need only return a success/failure result is
     * {@code Observable<Void>}.
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
     */
    public Observable<R> write(E value);

}
