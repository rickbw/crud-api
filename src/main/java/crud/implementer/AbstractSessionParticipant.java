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

import javax.annotation.Nonnull;

import crud.core.AsyncCloseable;
import crud.core.WritableResource;
import crud.core.DataSource;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;


/**
 * An abstract base class for a {@link DataSource} or {@link WritableResource}.
 *
 * @author Rick Warren
 */
public abstract class AbstractSessionParticipant implements AsyncCloseable {

    private @Nonnull final SessionWorker worker;


    /**
     * Subclasses should override {@link #doShutdown()} in preference to
     * this method.
     */
    @Override
    public Observable<Void> shutdown() {
        return this.worker.scheduleHot(new SessionWorker.Task<Void>() {
            @Override
            public void call(final Subscriber<? super Void> sub) throws Exception {
                doShutdown();
            }
        });
    }

    protected AbstractSessionParticipant(@Nonnull final SessionWorker worker) {
        this.worker = Objects.requireNonNull(worker);
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

    public final @Nonnull SessionWorker getWorker() {
        return this.worker;
    }

}
