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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Optional;

import rx.Observable;


/**
 * A heavy-weight connection to a middleware, such as a data store or
 * messaging system. A {@code DataBus} is usable by multiple threads.
 * Typically, an application will need only a single DataBus for a given
 * middleware active at one time.
 *
 * This class is analogous to a JMS {@link javax.jms.Connection}.
 *
 * @author Rick Warren
 */
@ThreadSafe
public interface DataBus extends AsyncCloseable {

    /**
     * When a {@link DataBus} is first initialized, it is in a "stopped"
     * state: no data may be consumed from it or produced to it. This method
     * puts the DataBus into a "started" state, allowing production and/or
     * consumption of data.
     * <p/>
     * The expected pattern is that the application will create a
     * {@link DataBus}, initialize all of its readable and writable resources, and
     * then finally start the bus.
     * <p/>
     * <b>Design Rationale</b>: Why does this method return {@link Void}
     * instead of {@link Observable Observable&lt;Void&gt;}? It is part of the
     * object-initialization phase of the application, and all object creation
     * methods are synchronous. Furthermore, once all readable and writable resources
     * have been created, there is nothing more that can be done with this bus
     * until starting has completed, so there seems to be little point in
     * allowing it to run asynchronously; most likely, every single caller
     * would choose to block on the result anyway.
     *
     * @throws IllegalStateException    If this {@link DataBus} was previously
     *                                  {@link #shutdown() shut down}.
     */
    public void start();

    /**
     * Access the set of data elements for reading, identified by the given
     * {@link ReadableResourceSet.Id}. If no such set exists, return
     * {@link Optional#absent()}.
     *
     * @throws MiddlewareException  If it is not possible to determine whether
     *                              such a data set exists.
     */
    public <K, E> Optional<ReadableResourceSet<K, E>> resources(ReadableResourceSet.Id<K, E> id);

    /**
     * Access the set of data elements for writing, identified by the given
     * {@link ReadableResourceSet.Id}. If no such set exists, return
     * {@link Optional#absent()}.
     *
     * @throws MiddlewareException  If it is not possible to determine whether
     *                              such a data set exists.
     */
    public <K, E, R> Optional<WritableResourceSet<K, E, R>> resources(WritableResourceSet.Id<K, E, R> id);

    /**
     * Return the set of {@link crud.core.Session.Ordering} values that would
     * result in a successful return from {@link #startSession(boolean)} or
     * {@link #startTransactedSession()} without an
     * {@link UnsupportedSessionOrderingException} being thrown. Because this
     * {@link DataBus} is always allowed to silently upgrade unordered to
     * ordered, the set will always contain at least the element
     * {@link crud.core.Session.Ordering#UNORDERED}.
     */
    public Set<Session.Ordering> getSupportedSessionOrderings();

    /**
     * Start a new {@link Session}. If {@code requireOrdering} is true, the
     * new Session must be <em>ordered</em>, and return
     * {@link crud.core.Session.Ordering#ORDERED} from
     * {@link Session#getOrdering()}. If {@code requireOrdering} is false,
     * the Session may be either ordered or unordered, depending on the
     * implementation.
     *
     * @throws UnsupportedSessionOrderingException  If ordering is required,
     *              but this {@link DataBus} only supports unordered Sessions.
     * @throws MiddlewareException                  If a Session could not be
     *              started for any other reason.
     */
    public @Nonnull Session startSession(boolean requireOrdering);

    /**
     * Start a new {@link TransactedSession}.
     *
     * @throws UnsupportedSessionOrderingException  If this {@link DataBus}
     *              does not support transacted Sessions.
     * @throws MiddlewareException                  If a Session could not be
     *              started for any other reason.
     */
    public @Nonnull TransactedSession startTransactedSession();

}
