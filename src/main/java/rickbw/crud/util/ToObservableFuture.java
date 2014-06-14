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
 *
 * Adapted from rx.internal.operators.OnSubscribeToObservableFuture from the
 * RxJava project, which is:
 *
 *      Copyright 2014 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */
package rickbw.crud.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.observables.BlockingObservable;
import rx.subscriptions.Subscriptions;


/**
 * Allows an {@link Observable} to wrap a {@link Future}. This ability is
 * built into RxJava in the form of {@link Observable#from(Future)}; however,
 * that implementation has a couple of problems, as of version 0.19:
 * <ul>
 *  <li>It exposes its {@link Subscriber}s to {@link ExecutionException}, an
 *      implementation artifact. As a result, a {@code Subscriber} or
 *      {@link Observer} that expects to handle a "{@code FooException}" will
 *      instead encounter an {@code ExecutionException} that <em>wraps</em> a
 *      {@code FooException}. Furthermore, because {@code ExecutionException}
 *      is checked, if the {@code Observable} is converted into a
 *      {@link BlockingObservable}, the calling code will encounter a
 *      {@link RuntimeException} that wraps the {@code ExecutionException}
 *      that wraps the {@code FooException}.</li>
 *  <li>It treats {@link CancellationException} and {@link InterruptedException}
 *      inconsistently. Both represent cases where external code request that
 *      execution be halted. However, the former results in a call to
 *      {@link Observer#onCompleted()}, the latter to
 *      {@link Observer#onError(Throwable)}. This implementation handles
 *      {@code InterruptedException} in the same way as
 *      {@code CancellationException}</li>
 * </ul>
 */
public class ToObservableFuture<T> implements OnSubscribe<T> {

    private final Future<? extends T> that;
    private final Action0 cancelOnUnsubscribe;


    public static <T> Observable<T> fromFuture(final Future<? extends T> that) {
        return Observable.create(new ToObservableFuture<>(that));
    }

    public static <T> Observable<T> fromFutureWithTimeout(
            final Future<? extends T> that,
            final long time,
            final TimeUnit unit) {
        return Observable.create(new TimedToObservableFuture<>(that, time, unit));
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        subscriber.add(Subscriptions.create(this.cancelOnUnsubscribe));
        try {
            // Don't block or propagate CancellationException if already unsubscribed:
            if (!subscriber.isUnsubscribed()) {
                final T value = futureGet();
                subscriber.onNext(value);
                subscriber.onCompleted();
            }
        } catch (final CancellationException cx) {
            /* CancellationException will not be passed to the final Subscriber
             * since it's already subscribed. If the Future is canceled in
             * another place, CancellationException will be still passed to
             * the final Subscriber.
             */
            if (subscriber.isUnsubscribed()) {
                // Refuse to emit onError if already unsubscribed:
                subscriber.onError(cx);
            } else {
                subscriber.onCompleted();
            }
        } catch (final InterruptedException ix) {
            /* No one above can throw this except Future.get(), so no need for
             * extra checks.
             */
            subscriber.onCompleted();
        } catch (final ExecutionException ex) {
            /* Subscriber shouldn't have to know or care that this Observable
             * is backed by a Future, and therefore wraps exceptions in
             * ExecutionException. Unwrap the original exception to expose the
             * proper type.
             */
            subscriber.onError(ex.getCause());
        } catch (final Throwable e) {
            subscriber.onError(e);
        }
    }

    /**
     * Get the value wrapped by the {@link Future}.
     *
     * @throws  TimeoutException See the subclass {@link TimedToObservableFuture},
     *          which throws this..
     */
    protected T futureGet()
    throws InterruptedException, ExecutionException, TimeoutException {
        return that.get();
    }

    private ToObservableFuture(final Future<? extends T> that) {
        this.that = that;
        this.cancelOnUnsubscribe = new Action0() {
            @Override
            public void call() {
                // If the Future is already completed, "cancel" does nothing.
                that.cancel(true);
            }
        };
    }


    private static class TimedToObservableFuture<T> extends ToObservableFuture<T> {
        private final long time;
        private final TimeUnit unit;

        public TimedToObservableFuture(final Future<? extends T> that, final long time, final TimeUnit unit) {
            super(that);
            this.time = time;
            this.unit = unit;
        }

        @Override
        protected T futureGet()
        throws InterruptedException, ExecutionException, TimeoutException {
            return super.that.get(time, unit);
        }
    }
}
