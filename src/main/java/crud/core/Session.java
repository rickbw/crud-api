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

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;


/**
 * A context for {@link Ordering ordering} data reads and writes. For
 * that ordering to be well defined, all of those reads and writes must take
 * place on a single thread. Indeed, if {@link #getOrdering()} returns
 * anything other than {@link Ordering#UNORDERED}, the application must call
 * all operations from a single thread, or that order will be impossible for
 * it to determine. Nevertheless, note that the Session implementation need
 * not do its work on the calling thread; it may do it in a separate worker
 * thread and return immediately.
 *
 * @author Rick Warren
 */
public interface Session {

    /**
     * Return the guarantee this {@link Session} makes with respect to the
     * order in which data elements will be observed.
     */
    public @Nonnull Ordering getOrdering();

    /**
     * Commit all data observations, both reads and writes, that previously
     * occurred in the context of this {@link Session}. They will not be
     * repeated. This action only takes effect if {@link #getOrdering()}
     * returns {@link Ordering#TRANSACTIONAL}; in all other cases, it will be
     * ignored, and the returned {@link Observable} will
     * {@link Observer#onCompleted() complete} successfully.
     * <p/>
     * The commit commences immediately with the call to this method; it
     * does not require the resulting {@link Observable} to be subscribed.
     * That Observable behaves as if {@link Observable#cache() cached}: the
     * same result will be emitted to any subscriber.
     *
     * @return  An {@link Observable} that will
     *          {@link Observer#onCompleted() complete} when the commit has
     *          taken place, or otherwise reflect any failure that occurred.
     */
    public Observable<Void> commit();

    /**
     * Roll back all data observations, both reads and writes, that previously
     * occurred in the context of this {@link Session}, such that they may be
     * repeated. This action only takes effect if {@link #getOrdering()}
     * returns {@link Ordering#TRANSACTIONAL}; in all other cases, it will be
     * ignored, and the returned {@link Observable} will
     * {@link Observer#onCompleted() complete} successfully.
     * <p/>
     * The roll-back commences immediately with the call to this method; it
     * does not require the resulting {@link Observable} to be subscribed.
     * That Observable behaves as if {@link Observable#cache() cached}: the
     * same result will be emitted to any subscriber.
     *
     * @return  An {@link Observable} that will
     *          {@link Observer#onCompleted() complete} when the roll-back has
     *          taken place, or otherwise reflect any failure that occurred.
     */
    public Observable<Void> rollback();

    /**
     * Stop all data observations in the context of this {@link Session}.
     * <p/>
     * The stop commences immediately with the call to this method; it
     * does not require the resulting {@link Observable} to be subscribed.
     * That Observable behaves as if {@link Observable#cache() cached}: the
     * same result will be emitted to any subscriber.
     * <p/>
     * <b>Design Rationale</b>: Why does this class not implement
     * {@link AutoCloseable}? Because that would encourage block-structured
     * code, such as with try-with-resources, and asynchronous data processing
     * is not naturally block structured. Applications would tend to
     * inadvertently shut down their {@link Session}s underneath their
     * asynchronously-running I/O operations.
     *
     * @return  An {@link Observable} that will
     *          {@link Observer#onCompleted() complete} when the roll-back has
     *          taken place, or otherwise reflect any failure that occurred.
     */
    public Observable<Void> stop();


    /**
     * Possible guarantees that particular {@link Session} instances may make
     * with respect to the order in which data elements are observed.
     */
    public static enum Ordering {
        /**
         * Data elements read or written in the context of the {@link Session}
         * have no particular ordering requirement relative to one another.
         * They may be delivered concurrently, such that race conditions cause
         * them to be reordered. They may also be reordered due to failures
         * and/or retries.
         */
        UNORDERED,
        /**
         * Data elements observed in the context of this {@link Session} have
         * a total ordering. However, that ordering cannot be abandoned or
         * observed again.
         */
        ORDERED,
        /**
         * The reading and writing of data elements in the context of this
         * {@link Session} are transactional: if a failure occurs along the
         * way, all modifications may be rolled back and subsequently
         * reconstructed.
         */
        TRANSACTIONAL
    }

}
