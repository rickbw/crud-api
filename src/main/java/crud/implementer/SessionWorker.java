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

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import crud.core.MiddlewareException;
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
public class SessionWorker {

    /**
     * All subscriptions run here.
     *
     * @see #scheduler
     */
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    /**
     * An Rx-friendly facade for the single-threaded {@link #executor}.
     */
    private final Scheduler scheduler = Schedulers.from(this.executor);

    /**
     * The first phase of the shutdown process is to call the
     * {@link #shutdown(Task, long, TimeUnit) shutdown} method. We only want
     * to do that once. We keep track of that here.
     *
     * @see #isFinalTaskRun
     */
    private final AtomicBoolean isShutdownCalled = new AtomicBoolean(false);
    /**
     * The second phase of the shutdown process is to run a final {@link Task}
     * provided by the application, which will presumably clean up any final
     * resources. Since we can't control when the application will subscribe
     * to any {@link Observable}s created by {@link #scheduleCold(Task)},
     * there is an unavoidable race condition, wherein the application can
     * attempt to schedule more work after the final task ha been scheduled,
     * but before the {@link #executor} is shut down. This flag is used to
     * work around that condition.
     *
     * @see #isShutdownCalled
     */
    private volatile boolean isFinalTaskRun = false;
    /**
     * All of these will be {@link Resource#shutdown() shut down} first
     * as part of this worker's {@link #shutdown(Task, long, TimeUnit)}
     * process.
     */
    private final Set<Resource<?>> preShutdownHooks = new LinkedHashSet<>();


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
        return doScheduleCold(task, isFinalTask);
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
        return doScheduleHot(task, isFinalTask);
    }

    /**
     * Add a listener that will be called prior to this {@link SessionWorker}
     * being {@link #shutdown(Task, long, TimeUnit) shut down}. The listener
     * will only be called once, just before shutting down.
     */
    public void addPreShutdownHook(@Nonnull final Resource<?> shutMeDown) {
        this.preShutdownHooks.add(shutMeDown);
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
        if (!this.isShutdownCalled.getAndSet(true)) {
            /* ATTN: Shutting down the hooks will likely cause more tasks to
             * be scheduled on this worker!
             */
            final Observable<Void> shutdownHookResults = AsyncResults.shutdownAll(this.preShutdownHooks);

            final boolean isFinalTask = true;
            final Observable<Void> taskResult = doScheduleHot(finalTask, isFinalTask);

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
            return Observable.concat(taskResult, shutdownHookResults, await);
        } else {
            return Observable.empty(); // do nothing
        }
    }

    private SessionWorker() {
        /* Private to prevent subclassing. We could just make the class final,
         * but that would prevent mocking as well. Fortunately, Mockito can
         * call a private constructor reflectively.
         */
    }

    private <T> Observable<T> doScheduleHot(final Task<T> task, final boolean isFinalTask) {
        final Observable<T> obs = doScheduleCold(task, isFinalTask).cache();
        /* The no-argument subscribe() does not handle errors, so
         * materialize() so that it won't see any.
         */
        obs.materialize().subscribe();
        return obs;
    }

    private <T> Observable<T> doScheduleCold(final Task<T> task, final boolean isFinalTask) {
        final Observable.OnSubscribe<T> onSubscribe = new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> sub) {
                if (isFinalTask) {
                    SessionWorker.this.isFinalTaskRun = true;
                } else if (SessionWorker.this.isFinalTaskRun) {
                    throw new RejectedExecutionException("Session already shut down");
                }

                try {
                    task.call(sub);
                    sub.onCompleted();
                } catch (final MiddlewareException mx) {
                    sub.onError(mx);
                } catch (final Exception ex) {
                    sub.onError(new MiddlewareException(ex.getMessage(), ex));
                }
            }
        };
        return Observable.create(onSubscribe).subscribeOn(this.scheduler);
    }


    /**
     * An asynchronous task to be scheduled by a {@link SessionWorker}.
     * It will be wrapped by an instance of {@link rx.Observable.OnSubscribe},
     * and execution of {@link Observer#onCompleted()} and
     * {@link Observer#onError(Throwable)} will be taken care of on behalf of
     * the task; it need only invoke {@link Observer#onNext(Object)}.
     */
    public static interface Task<T> {
        void call(Subscriber<? super T> sub) throws Exception;
    }

}
