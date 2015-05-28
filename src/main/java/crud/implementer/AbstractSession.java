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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import crud.core.Session;
import rx.Observable;


public abstract class AbstractSession extends AbstractAsyncCloseable implements Session {

    private @Nonnull final SessionWorker worker;
    private @Nonnull final Session.Ordering ordering;

    private final Callable<Void> shutdownTask = new Callable<Void>() {
        @Override
        public Void call() throws Exception {
            doShutdown();
            return null;
        }
    };


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
     * @see #shutdown()
     */
    protected void doShutdown() {
        // do nothing
    }

    protected final SessionWorker getWorker() {
        return this.worker;
    }

}
