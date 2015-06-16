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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import crud.core.AsyncCloseable;
import crud.core.MiddlewareException;
import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Action1;


/**
 * Facilitates the execution and shutdown of (possible asynchronous)
 * {@link Task}s on behalf of other classes. An important aspect of this are
 * the "shutdown hooks": instances of {@link AsyncCloseable} (or a subtype)
 * that will be called prior to the shutdown of this WorkerDelegate itself.
 *
 * @see #addPreShutdownHook(AsyncCloseable)
 *
 * @author Rick Warren
 */
@ThreadSafe
/*package*/ final class WorkerDelegate {

    /**
     * All {@link Task}s run here.
     */
    private final Scheduler scheduler;

    /**
     * The first phase of the shutdown process is to call the
     * {@link #shutdown(Task, long, TimeUnit) shutdown} method. We only want
     * to do that once. We keep track of that here.
     *
     * @see #hasFinalTaskBeenRun
     */
    private final AtomicBoolean hasShutdownBeenCalled = new AtomicBoolean(false);
    /**
     * The second phase of the shutdown process is to run a final {@link Task}
     * provided by the application, which will presumably clean up any final
     * resources. Since we can't control when the application will subscribe
     * to any {@link Observable}s created by {@link #scheduleCold(Task)},
     * there is an unavoidable race condition, wherein the application can
     * attempt to schedule more work after the final task ha been scheduled,
     * but before the {@link #scheduler} is shut down. This flag is used to
     * work around that condition.
     *
     * @see #hasShutdownBeenCalled
     */
    private volatile boolean hasFinalTaskBeenRun = false;
    /**
     * All of these will be {@link AsyncCloseable#shutdown() shut down} first
     * as part of this worker's {@link #shutdown(Task, long, TimeUnit)}
     * process.
     */
    private final Set<AsyncCloseable> preShutdownHooks = new LinkedHashSet<>();


    public WorkerDelegate(@Nonnull final Scheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    /**
     * Wrap the given {@link Action1 action} in an {@link Observable} and
     * schedule its subscription to run on the {@link Scheduler} encapsulated
     * by this {@link SessionWorker worker}. This method only creates the
     * {@link Observable}; it does not subscribe to it.
     *
     * @see #scheduleHot(Task, boolean)
     */
    public <T> Observable<T> scheduleCold(final Task<T> task, final boolean isFinalTask) {
        final Observable.OnSubscribe<T> onSubscribe = new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> sub) {
                if (isFinalTask) {
                    WorkerDelegate.this.hasFinalTaskBeenRun = true;
                } else if (WorkerDelegate.this.hasFinalTaskBeenRun) {
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
     * Wrap the given {@link Action1 action} in an {@link Observable} and
     * schedule its subscription to run on the {@link Scheduler} encapsulated
     * by this {@link SessionWorker worker}. Then
     * {@link Observable#cache() cache} the result, and begin the
     * subscription. This allows the subscription action to begin immediately,
     * but allows the caller to see every resulting value. Note that the use
     * of cache() assumes that the number of those results is relatively
     * small.
     *
     * @see #scheduleCold(Task, boolean)
     */
    public <T> Observable<T> scheduleHot(final Task<T> task, final boolean isFinalTask) {
        final Observable<T> obs = scheduleCold(task, isFinalTask).cache();
        /* The no-argument subscribe() does not handle errors, so
         * materialize() so that it won't see any.
         */
        obs.materialize().subscribe();
        return obs;
    }

    /**
     * Add a listener that will be called prior to this {@link WorkerDelegate}
     * being {@link #shutdown(Task) shut down}. The listener
     * will only be called once, just before shutting down.
     *
     * @throws IllegalStateException    If this method is called after
     *              {@link #shutdown(Task)} itself.
     */
    public void addPreShutdownHook(@Nonnull final AsyncCloseable shutMeDown) {
        /* Ugly lock double checking with the following rationale:
         *  1. If shutdown() was called before this method, fail fast with the
         *     first check of hasShutdownBeenCalled.
         *  2. ...But that alone would have a race condition, if shutdown()
         *     were called after the check but before (or worse, during) the
         *     call to add(). Therefore, serialize access to the preShutdownHooks, and
         *     check hasShutdownBeenCalled again. If this method won the serialization
         *     race, shutdown() won't visit any preShutdownHooks until the new one has
         *     been added. But if shutdown() won the race, then the previous
         *     preShutdownHooks have already been shut down; don't add a new one.
         */
        if (!this.hasShutdownBeenCalled.get()) {
            synchronized (this.preShutdownHooks) {
                if (!this.hasShutdownBeenCalled.get()) {
                    this.preShutdownHooks.add(shutMeDown);
                } else {
                    throw new IllegalStateException("already shut down");
                }
            }
        } else {
            throw new IllegalStateException("already shut down");
        }
    }

    /**
     * {@link #scheduleHot(Task, boolean) Schedule} the given
     * task after shutting down the
     * {@link #addPreShutdownHook(AsyncCloseable) pre-shutdown hooks}. The
     * resulting {@link Observable} will emit one of the following:
     * <ol>
     *  <li>Any {@link Observer#onError(Throwable) error} emitted by the given
     *      final task. (This task will be run after all
     *      {@link #addPreShutdownHook(AsyncCloseable) pre-shutdown hooks}
     *      have been shut down, but any error from it will receive precedence
     *      with respect to reporting, because it is likely to be the most
     *      relevant to the caller.)</li>
     *  <li>The first {@link Observer#onError(Throwable) error}, if any,
     *      emitted by any of the
     *      {@link #addPreShutdownHook(AsyncCloseable) pre-shutdown hooks}.
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
    public Observable<Void> shutdown(@Nonnull final Task<Void> finalTask) {
        /* We do need to actually proactively avoid shutting down again,
         * because even though shutting down the ExecutorService is
         * idempotent, we won't be able (and don't want) to run the "final"
         * task again, or call the pre-shutdown hooks again.
         */
        if (!this.hasShutdownBeenCalled.getAndSet(true)) {
            /* ATTN: Shutting down the hooks will likely cause more tasks to
             * be scheduled on this worker!
             */
            final Observable<Void> shutdownHookResults = runPreShutdownHooks();
            final Observable<Void> taskResult = scheduleHot(
                    finalTask,
                    true /*it's the final task*/);

            /* Concat in the order described in the JavaDoc above.
             * Per our conventions, no Observable<Void> should ever emit any
             * elements, so concat'ing them together should yield simply an
             * onCompleted() (if all complete successfully) or an onError
             * (carrying the first failure that was seen). However, just in case
             * someone does try to emit any data elements, call ignoreElements()
             * to eliminate them.
             */
            return Observable.concat(taskResult, shutdownHookResults).ignoreElements();
        } else {
            return Observable.empty(); // do nothing
        }
    }

    private Observable<Void> runPreShutdownHooks() {
        final List<Observable<Void>> results = new ArrayList<>();
        /* Synchronize to avoid clobbering the collection while a new
         * member is being added.
         */
        synchronized (this.preShutdownHooks) {
            for (final AsyncCloseable shutMeDown : this.preShutdownHooks) {
                /* Per the contract of shutdown(), these result Observables
                 * are hot. We don't need to subscribe to them here to make
                 * the shutdowns happen.
                 */
                results.add(shutMeDown.shutdown());
            }
        }
        return Observable.concat(Observable.from(results));
    }

}
