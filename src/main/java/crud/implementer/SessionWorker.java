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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import crud.core.Resource;
import crud.core.Session;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;
import rx.schedulers.Schedulers;


/**
 * Runs tasks in a background thread on behalf of {@link Session}
 * implementations.
 *
 * @author Rick Warren
 */
@ThreadSafe
public class SessionWorker {

    /**
     * All subscriptions run here.
     *
     * @see #scheduler
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final WorkerDelegate delegate = new WorkerDelegate(Schedulers.from(this.executor));


    public static SessionWorker create() {
        return new SessionWorker();
    }

    /**
     * Wrap the given {@link Action1 action} in an {@link Observable} and
     * schedule its subscription to run on the {@link Scheduler} encapsulated
     * by this {@link SessionWorker worker}. This method only creates the
     * {@link Observable}; it does not subscribe to it.
     *
     * @see #scheduleHot(Task)
     */
    public <T> Observable<T> scheduleCold(@Nonnull final Task<T> task) {
        final boolean isFinalTask = false;
        return this.delegate.scheduleCold(task, isFinalTask);
    }

    /**
     * Wrap the given {@link Action1 action} in an {@link Observable} and
     * schedule its subscription to run on the {@link Scheduler} encapsulated
     * by this {@link SessionWorker worker}. Then
     * {@link Observable#cache() cache} the result, and begin the
     * subscription. This allows the subscription action to begin immediately,
     * but allows the caller to see every resulting value. Note that the use
     * of cache() assumes that the number of those results is relatively
     * small.
     *
     * @see #scheduleCold(Task)
     */
    public <T> Observable<T> scheduleHot(@Nonnull final Task<T> task) {
        final boolean isFinalTask = false;
        return this.delegate.scheduleHot(task, isFinalTask);
    }

    /**
     * Add a listener that will be called prior to this {@link SessionWorker}
     * being {@link #shutdown(Task, long, TimeUnit) shut down}. The listener
     * will only be called once, just before shutting down.
     */
    public void addPreShutdownHook(@Nonnull final Resource<?> shutMeDown) {
        this.delegate.addPreShutdownHook(shutMeDown);
    }

    /**
     * {@link #scheduleHot(Task) Schedule} the given
     * task, then stop accepting any new tasks, and initiate
     * the termination of this worker's background thread. The resulting
     * {@link Observable} will emit one of the following:
     * <ol>
     *  <li>Any {@link Observer#onError(Throwable) error} emitted by the given
     *      final task. (This task will be run after all
     *      {@link #addPreShutdownHook(Resource) pre-shutdown hooks}
     *      have been shut down, but any error from it will receive precedence
     *      with respect to reporting, because it is likely to be the most
     *      relevant to the caller.)</li>
     *  <li>The first {@link Observer#onError(Throwable) error}, if any,
     *      emitted by any of the
     *      {@link #addPreShutdownHook(Resource) pre-shutdown hooks}.
     *      (These hooks are shut down before the final task runs, but their
     *      errors are not allowed to hide any errors from that task.)</li>
     *  <li>A {@link TimeoutException} if all scheduled tasks fail to complete
     *      within the given duration.</li>
     *  <li>An {@link InterruptedException} if the shutdown process is
     *      interrupted from another thread.</li>
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
    public Observable<Void> shutdown(
            @Nonnull final Task<Void> finalTask,
            final long waitDuration, @Nonnull final TimeUnit waitUnit) {
        /* No need to explicitly check for repeated invocations, since our
         * WorkerDelegate helper does that for us, with respect to the
         * pre-shutdown hooks and the final task. Shutting down an
         * ExecutorService is idempotent.
         *
         * ATTN: Shutting down the hooks will likely cause more tasks to
         * be scheduled on this delegate!
         */
        final Observable<Void> taskResults = this.delegate.shutdown(finalTask);

        this.executor.shutdown();   // non-blocking

        final Observable<Void> await = Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> sub) {
                try {
                    final boolean completed = SessionWorker.this.executor.awaitTermination(waitDuration, waitUnit);
                    if (completed) {
                        sub.onCompleted();
                    } else {
                        sub.onError(new TimeoutException(
                                "Shutdown timed out after " + waitDuration + " " + waitUnit.toString().toLowerCase()));
                    }
                } catch (final InterruptedException ix) {
                    sub.onError(ix);
                }
            }
        });

        // Concat in the order described in the JavaDoc above:
        return Observable.concat(taskResults, await);
    }

    private SessionWorker() {
        /* Private to prevent subclassing. We could just make the class final,
         * but that would prevent mocking as well. Fortunately, Mockito can
         * call a private constructor reflectively.
         */
    }

}
