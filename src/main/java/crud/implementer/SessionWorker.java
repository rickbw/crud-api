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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import crud.core.MiddlewareException;
import crud.core.Session;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;


/**
 * Runs tasks in a background thread on behalf of {@link Session}
 * implementations.
 *
 * @author Rick Warren
 */
public final class SessionWorker {

    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();


    /**
     * Schedule the given task to run on this worker's asynchronous thread.
     * Return a {@link Observable#cache() cached} {@link Observable} that
     * will emit the success or error result of the task to all subscribers.
     * <p/>
     * If {@link #shutdown(Callable, long, TimeUnit)} was already called, the
     * resulting {@link Observable} will emit
     * {@link RejectedExecutionException}.
     */
    public Observable<Void> submit(@Nonnull final Callable<Void> task) {
        if (!this.stopped.get()) {
            return doSubmit(task);
        } else {
            return Observable.error(new RejectedExecutionException("Already stopped"));
        }
    }

    /**
     * Stop accepting new tasks via {@link #submit(Callable)}, and initiate
     * the termination of this worker's background thread. The resulting
     * {@link Observable} will emit {@link Observer#onCompleted()} once the
     * termination is complete, a {@link TimeoutException} if it fails to
     * complete within the given duration, or {@link InterruptedException} if
     * the shutdown is interrupted.
     *
     * @param finalTask The caller should perform any of its own cleanup in
     *                  this task, scheduled here as opposed to in
     *                  {@link #submit(Callable)} to avoid race conditions on
     *                  shutdown.
     */
    public Observable<Void> shutdown(
            final @Nonnull Callable<Void> finalTask,
            final long waitDuration, @Nonnull final TimeUnit waitUnit) {
        if (!this.stopped.getAndSet(true)) {
            final Observable<Void> taskResult = doSubmit(finalTask);
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
            /* The final task is submitted, and the Executor is shut down, no
             * matter what happens now. If the app decides to observe the
             * result, it will see first the result of the final task it
             * submitted, which in the success case will be empty. In the
             * failure case, it will observe the failure of its own task. If
             * its task succeeds, then it will observe the termination of the
             * Executor, which should again be empty, and non-blocking, since
             * we know at that point there are no more tasks to wait for. But
             * if it does fail for some reason, that app will have a chance
             * to observe that.
             */
            return Observable.concat(taskResult, await);
        } else {
            return Observable.empty(); // do nothing
        }
    }

    private Observable<Void> doSubmit(final Callable<Void> task) {
        final Observable<Void> result = Observable.create(actionOf(task)).cache();
        /* Start a subscription now, so that the given task is immediately
         * scheduled. The no-argument subscribe() does not handle errors, so
         * materialize so that it won't see any.
         */
        result.materialize().subscribe();
        return result;
    }

    private Observable.OnSubscribe<Void> actionOf(final Callable<Void> task) {
        return new Observable.OnSubscribe<Void>() {
            @Override
            public void call(final Subscriber<? super Void> sub) {
                SessionWorker.this.executor.submit(runAndNotify(task, sub));
            }
        };
    }

    private static Runnable runAndNotify(
            final Callable<Void> runMe,
            final Subscriber<? super Void> notifyMe) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    runMe.call();
                    notifyMe.onCompleted();
                } catch (final RuntimeException rex) {
                    /* RuntimeExceptions are assumed to indicate program bugs.
                     * Report them to the subscriber as-is. This clause will
                     * also include MiddlewareExceptions as-is, which is
                     * desirable.
                     */
                    notifyMe.onError(rex);
                } catch (final Exception ex) {
                    /* Any checked exception is assumed to represent a
                     * middleware-specific failure condition, and so is mapped
                     * to MiddlewareException.
                     *
                     * TODO: Provide a pluggable exception-translation
                     * capability.
                     */
                    notifyMe.onError(new MiddlewareException(ex.getMessage(), ex));
                }
            }
        };
    }

}
