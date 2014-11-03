/* Copyright 2014 Rick Warren
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
package crud.session;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.collect.ImmutableList;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func0;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;


/**
 * A {@code Session} forces subscriptions to a sequence of {@link Observable}s
 * to occur in sequential order, such that the subscription to the
 * {@code [n]}th {@code Observable} will not be executed until the
 * {@code [n-1]}th {@code Observable} has terminated, whether successfully or
 * unsuccessfully. This pattern should be familiar to those familiar with
 * "sessions" in Hibernate, JMS, and other technologies.
 *
 * TODO: Upon failure, execution should not continue.
 *
 * A {@code Session} exists in one of three states:
 * <ol>
 *  <li>When a {@code Session} is created, {@code Observable}s can be
 *      {@link #attach(Func0) attached} to it, and they will be scheduled to
 *      run after all previously attached {@code Observable}s have terminated.
 *      </li>
 *  <li>After all desired {@code Observable}s have been attached, the
 *      {@code Session} should be {@link #seal() sealed}. A sealed
 *      {@code Session} continues to execute the {@code Observable}s
 *      already attached to it, but it does not accept further attachments.
 *      </li>
 *  <li>While in either of the two previous states, a {@code Session} may be
 *      {@link #cancel() cancelled}. Cancellation halts the execution of all
 *      pending {@link Observable}s. These are returned to the application
 *      by the {@link #cancel()} operation.</li>
 * </ol>
 *
 * @see #attach(Func0)
 */
public class Session {

    private final BlockingQueue<ConnectableObservable<?>> pending;

    private final ExecutorService executor;
    private final Scheduler subscriptionScheduler;
    private final ConnectableObservable<?> shutdownObservable;

    private final AtomicBoolean sealed = new AtomicBoolean(false);
    private final AtomicBoolean connectOnDefer = new AtomicBoolean(true);

    private final long enqueueTimeout;
    private final TimeUnit enqueueTimeoutUnits;
    private final long connectNextTimeout;
    private final TimeUnit connectNextTimeoutUnit;


    public Session() {
        // TODO: Inject implementation of Queue
        this.pending = new LinkedBlockingQueue<>();

        // TODO: Inject attributes for Executor
        this.executor = Executors.newSingleThreadExecutor();
        this.subscriptionScheduler = Schedulers.from(this.executor);
        this.shutdownObservable = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(final Subscriber<Object> subscriber) {
                executor.shutdown();
                subscriber.onCompleted();
            }
        }).publish();

        // TODO: Inject timeouts
        this.enqueueTimeout = Long.MAX_VALUE;
        this.enqueueTimeoutUnits = TimeUnit.NANOSECONDS;
        this.connectNextTimeout = Long.MAX_VALUE;
        this.connectNextTimeoutUnit = TimeUnit.NANOSECONDS;
    }

    /**
     * Attach an {@link Observable} to this {@code Session}, such that the
     * {@code Observable}'s subscriptions will be deferred until all
     * previously attached {@code Observable}s have terminated. No
     * {@code Observable} will be retrieved from the {@code observableFactory}
     * until that precondition occurs.
     *
     * If this {@code Session} has already been sealed or cancelled, no
     * {@code Observable} from the {@code observableFactory} will be enqueued,
     * and this method will return an {@code Observable} that wraps a
     * {@link RejectedExecutionException}.
     */
    public <T> Observable<T> attach(final Func0<? extends Observable<? extends T>> observableFactory) {
        if (this.sealed.get()) {
            return Observable.error(new RejectedExecutionException("Session is already sealed or cancelled"));
        }

        final ConnectableObservable<T> enqueueMe = Observable
                // Don't get the original Observable from the factory until subscribe():
                .defer(observableFactory)
                // Run subscribe() on another thread, in case it blocks:
                .subscribeOn(this.subscriptionScheduler)
                // After subscribe() completes, unblock (connect()) next Observable:
                .lift(new ConnectNextOnTermination<T>())
                // Don't process subscribe()s until unblocked (connect()ed):
                .publish();

        final boolean enqueued = offerToQueue(enqueueMe);
        if (enqueued) {
            // For first Observable only, manually connect() it:
            final boolean connectFirst = this.connectOnDefer.getAndSet(false);
            if (connectFirst) {
                connectNext();
            }
            return enqueueMe;
        } else {
            return offerToQueueFailure();
        }
    }

    /**
     * Indicate that the last of the {@link Observable}s to be subscribed
     * within this {@link Session} have been {@link #attach(Func0) attached}.
     * Prevent further {@code Observable}s from being attached.
     *
     * @return  An {@code Observable} that will call
     *          {@link Observer#onCompleted()} once the last pending
     *          {@code Observable} has terminated.
     */
    public Observable<?> seal() {
        final boolean previouslySealed = this.sealed.getAndSet(true);
        if (!previouslySealed) {
            // Will get connect()ed once all previous Observables have run:
            this.shutdownObservable.subscribe();

            final boolean enqueued = offerToQueue(this.shutdownObservable);
            if (!enqueued) {
                /* XXX: This is bad: we've already marked this Session as
                 * sealed, and possibly failed calls to attach() in the
                 * mean time, and now we find that we're not capable of
                 * sealing after all. Back out the deal and inform the
                 * caller of the problem.
                 */
                this.sealed.set(false);
                return offerToQueueFailure();
            }
        }
        return this.shutdownObservable;
    }

    /**
     *
     */
    public List<Observable<?>> cancel() {
        this.sealed.set(true);
        this.executor.shutdownNow();
        /* TODO: Figure out how to make all pending Observables fail with
         * CancellationException or similar. The most straightforward way
         * would be to iterate over all of them, calling connect() followed
         * Subscription.cancel(). However, that would be unsafe, since any
         * synchronous Observable would execute before returning the
         * Subscription, so we wouldn't actually be cancelling them; we would
         * be running them.
         */
        return ImmutableList.<Observable<?>>copyOf(this.pending);
    }

    /**
     * Call {@link ConnectableObservable#connect() connect} on the next
     * pending {@link Observable}, marking it as "runnable". If no "next"
     * item becomes available in the time allowed, this session is cancelled,
     * and the un-subscribed {@code Observable}s are returned. If this method
     * completes successfully, it returns an empty list.
     */
    private void connectNext() {
        try {
            final ConnectableObservable<?> next = pending.poll(this.connectNextTimeout, this.connectNextTimeoutUnit);
            if (next != null) {
                /* XXX: connect() returns a Subscription; what to do with it?
                 * The original party who tried to subscribe to the Observable
                 * isn't available to pass it to.
                 */
                next.connect();
                return;
            }
        } catch (final InterruptedException ix) {
            // Handled below, uniformly with next == null:
        }

        final List<Observable<?>> cancelled = cancel();
        if (!cancelled.isEmpty()) {
            // TODO: report failures
        }
    }

    private <T> boolean offerToQueue(final ConnectableObservable<T> enqueueMe) {
        try {
            final boolean enqueued = pending.offer(
                    enqueueMe,
                    this.enqueueTimeout, this.enqueueTimeoutUnits);
            return enqueued;
        } catch (final InterruptedException ix) {
            return false;
        }
    }

    private <T> Observable<T> offerToQueueFailure() {
        return Observable.error(new IllegalStateException(
                "Unable to enqueue Observable after "
                        + this.enqueueTimeout + " "
                        + this.enqueueTimeoutUnits.toString().toLowerCase()));
    }


    private final class ConnectNextOnTermination<T> implements Observable.Operator<T, T> {
        @Override
        public Subscriber<T> call(final Subscriber<? super T> subscriber) {
            final Subscriber<T> wrapper = new Subscriber<T>() {
                @Override
                public void onNext(final T next) {
                    subscriber.onNext(next);
                }

                @Override
                public void onCompleted() {
                    subscriber.onCompleted();
                    connectNext();
                }

                @Override
                public void onError(final Throwable ex) {
                    subscriber.onError(ex);
                    connectNext();
                }
            };
            subscriber.add(wrapper);
            return wrapper;
        }
    }

}
