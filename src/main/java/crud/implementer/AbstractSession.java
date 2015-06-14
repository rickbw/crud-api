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
package crud.implementer;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import crud.core.Session;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;


public abstract class AbstractSession extends AbstractAsyncCloseable implements Session {

    private @Nonnull final SessionWorker worker;
    private @Nonnull final Session.Ordering ordering;

    private final SessionWorker.Task<Void> shutdownTask = new SessionWorker.Task<Void>() {
        @Override
        public void call(final Subscriber<? super Void> sub) throws Exception {
            doShutdown();
        }
    };


    /**
     * Provided for use by subclasses and their clients.
     * <p/>
     * <em>ATTN</em>: This method is not declared {@code final} in order to
     * support mocking in unit tests. Nevertheless, it is not intended for
     * overriding, and the behavior in that case is unspecified.
     */
    public @Nonnull SessionWorker getWorker() {
        return this.worker;
    }

    @Override
    public Session.Ordering getOrdering() {
        return this.ordering;
    }

    /**
     * Subclasses should override {@link #doShutdown()} in preference to
     * this method.
     */
    @Override
    public Observable<Void> shutdown() {
        return this.worker.shutdown(this.shutdownTask, Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    protected AbstractSession(
            @Nonnull final SessionWorker worker,
            @Nonnull final Session.Ordering ordering) {
        this.worker = Objects.requireNonNull(worker);
        this.ordering = Objects.requireNonNull(ordering);
    }

    protected AbstractSession(@Nonnull final Session.Ordering ordering) {
        this(new SessionWorker(), ordering);
    }

    /**
     * Subclasses should override this method to perform any shutdown task
     * they have to do. It will be run in the context of the
     * {@link SessionWorker}'s thread.
     *
     * By default, this method does nothing.
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     *
     * @see #shutdown()
     */
    protected void doShutdown() throws Exception {
        // do nothing
    }

}
