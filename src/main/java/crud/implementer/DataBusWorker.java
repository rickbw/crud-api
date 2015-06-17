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

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import crud.core.DataBus;
import crud.core.Session;
import rx.Observable;
import rx.Observer;
import rx.schedulers.Schedulers;


/**
 * Manages lifecycle concerns on behalf of {@link DataBus} implementations.
 *
 * @author Rick Warren
 */
@ThreadSafe
public class DataBusWorker {

    private final WorkerDelegate delegate = new WorkerDelegate(Schedulers.immediate());


    public static DataBusWorker create() {
        return new DataBusWorker();
    }

    /**
     * Add a listener that will be called prior to this {@link DataBusWorker}
     * being {@link #shutdown(Task) shut down}. The listener will only be
     * called once, just before shutting down.
     *
     * @throws IllegalStateException    If this method is called after
     *              {@link #shutdown(Task)} itself.
     */
    public void addPreShutdownHook(@Nonnull final Session shutMeDown) {
        this.delegate.addPreShutdownHook(shutMeDown);
    }

    /**
     * Schedule the given task after shutting down the
     * {@link #addPreShutdownHook(Session) pre-shutdown hooks}. The
     * resulting {@link Observable} will emit one of the following:
     * <ol>
     *  <li>Any {@link Observer#onError(Throwable) error} emitted by the given
     *      final task. (This task will be run after all
     *      {@link #addPreShutdownHook(Session) pre-shutdown hooks}
     *      have been shut down, but any error from it will receive precedence
     *      with respect to reporting, because it is likely to be the most
     *      relevant to the caller.)</li>
     *  <li>The first {@link Observer#onError(Throwable) error}, if any,
     *      emitted by any of the
     *      {@link #addPreShutdownHook(Session) pre-shutdown hooks}.
     *      (These hooks are shut down before the final task runs, but their
     *      errors are not allowed to hide any errors from that task.)</li>
     *  <li>{@link Observer#onCompleted()} once the termination is complete,
     *      if no errors occurred.</li>
     * </ol>
     * <p/>
     * This method only operates once. Calling it additional times has no
     * effect, and will return an {@link Observable} that emits
     * {@link Observer#onCompleted()}.
     *
     * @param finalTask The caller should perform any of its own cleanup in
     *                  this task, scheduled here to avoid race conditions.
     */
    public Observable<Void> shutdown(final Task<Void> finalTask) {
        return this.delegate.shutdown(finalTask);
    }

    private DataBusWorker() {
        /* Private to prevent subclassing. We could just make the class final,
         * but that would prevent mocking as well. Fortunately, Mockito can
         * call a private constructor reflectively.
         */
    }

}
