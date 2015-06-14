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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;

import crud.core.MiddlewareException;
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
 * <p/>
 * <em>ATTN</em>: This class is not declared {@code final} in order to support
 * mocking in unit tests. Nevertheless, it is not intended for subclassing,
 * and the behavior in that case is unspecified.
 *
 * @author Rick Warren
 */
public class SessionWorker {

    private final AtomicBoolean stopped = new AtomicBoolean(false);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Scheduler scheduler = Schedulers.from(this.executor);


    /**
     * Wrap the given {@link Action1 action} in an {@link Observable} and
     * schedule its subscription to run on the {@link Scheduler} encapsulated
     * by this {@link SessionWorker worker}. This method only creates the
     * {@link Observable}; it does not subscribe to it.
     *
     * @see #subscribeHot(Observable)
     */
    public <T> Observable<T> scheduleCold(@Nonnull final Task<T> task) {
        final Observable.OnSubscribe<T> onSubscribe = new Observable.OnSubscribe<T>() {
            @Override
            public void call(final Subscriber<? super T> sub) {
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
     * @see #scheduleCold(Task)
     * @see #subscribeHot(Observable)
     */
    public <T> Observable<T> scheduleHot(@Nonnull final Task<T> task) {
        final Observable<T> result = scheduleCold(task).cache();
        doSubscribeHot(result);
        return result;
    }

    /**
     * Start subscribing to the given {@link Observable} on the
     * {@link Scheduler} encapsulated by this {@link SessionWorker worker}.
     * Note that the caller may miss some results if it does not e.g.
     * {@link Observable#share() share} the input Observable before calling
     * this method.
     */
    public <T> void subscribeHot(final Observable<T> obs) {
        doSubscribeHot(obs.subscribeOn(this.scheduler));
    }

    /**
     * {@link #scheduleHot(Task) Schedule} the given
     * task, then stop accepting any new tasks, and initiate
     * the termination of this worker's background thread. The resulting
     * {@link Observable} will emit {@link Observer#onCompleted()} once the
     * termination is complete, a {@link TimeoutException} if it fails to
     * complete within the given duration, or {@link InterruptedException} if
     * the shutdown is interrupted.
     *
     * @param finalTask The caller should perform any of its own cleanup in
     *                  this task, scheduled here to avoid race conditions.
     */
    public Observable<Void> shutdown(
            @Nonnull final Task<Void> finalTask,
            final long waitDuration, @Nonnull final TimeUnit waitUnit) {
        if (!this.stopped.getAndSet(true)) {
            final Observable<Void> taskResult = scheduleHot(finalTask);
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

    private static <T> void doSubscribeHot(final Observable<T> obs) {
        /* The no-argument subscribe() does not handle errors, so
         * materialize() so that it won't see any.
         */
        obs.materialize().subscribe();
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
