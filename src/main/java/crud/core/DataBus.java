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

import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Optional;

import rx.Observable;
import rx.Observer;


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
public interface DataBus {

    /**
     * When a {@link DataBus} is first initialized, it is in a "stopped"
     * state: no data may be consumed from it or produced to it. This method
     * puts the DataBus into a "started" state, allowing production and/or
     * consumption of data.
     * <p/>
     * The expected pattern is that the application will create a
     * {@link DataBus}, initialize all of its data sources and sinks, and
     * then finally start the bus.
     * <p/>
     * <b>Design Rationale</b>: Why does this method return {@link Void}
     * instead of {@link Observable Observable&lt;Void&gt;}? It is part of the
     * object-initialization phase of the application, and all object creation
     * methods are synchronous. Furthermore, once all data sources and sinks
     * have been created, there is nothing more that can be done with this bus
     * until starting has completed, so there seems to be little point in
     * allowing it to run asynchronously; most likely, every single caller
     * would choose to block on the result anyway.
     *
     * @throws IllegalStateException    If this {@link DataBus} was previously
     *                                  {@link #shutdown() shut down}.
     */
    public void start();

    public <K, E> Optional<DataSet<K, E>> dataSet(DataSetId<K, E> id);

    /**
     * Initiate the (possibly asynchronous) shutdown of this {@link DataBus}.
     * Once this DataBus is shut down, it will no longer be possible to
     * consume or produce data. This state cannot be undone.
     * <p/>
     * <b>Design Rationale</b>: Why does this class not implement a
     * synchronous shutdown analogous to the synchronous {@link #start()},
     * and perhaps even implement {@link AutoCloseable}? Because that would
     * encourage block-structured code, such as with try-with-resources, and
     * asynchronous data processing is not naturally block structured.
     * Applications would tend to inadvertently shut down their
     * {@link DataBus} underneath their asynchronously-running I/O operations.
     *
     * @return  A zero-element {@link Observable}. It will report
     *          {@link Observer#onCompleted() onCompleted} on successful
     *          shutdown, or {@link Observer#onError(Throwable) onError} if
     *          an error occurred.
     */
    public Observable<Void> shutdown();

}
