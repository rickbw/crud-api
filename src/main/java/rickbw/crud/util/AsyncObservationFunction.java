/* Copyright 2013–2014 Rick Warren
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

package rickbw.crud.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;


/**
 * Dispatches an object returned from a {@link Callable} to {@link Observer}s
 * asynchronously using an {@link ExecutorService}. This class is designed to
 * eliminate boilerplate code when working with asynchronous
 * {@link Observable} implementations.
 */
public final class AsyncObservationFunction<T> implements Observable.OnSubscribe<T> {

    private final Callable<T> provider;
    private final ExecutorService executor;


    /**
     * The given data provider will be executed on the given executor service
     * each time that {@link #call(Subscriber)} is invoked.
     */
    public AsyncObservationFunction(final Callable<T> provider, final ExecutorService executor) {
        this.provider = Preconditions.checkNotNull(provider);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        final Runnable task = new ObservationTask(subscriber);
        final Future<?> taskResult = this.executor.submit(task);
        final Subscription subscription = Subscriptions.from(taskResult);
        subscriber.add(subscription);
    }

    private final class ObservationTask implements Runnable {
        private final Subscriber<? super T> subscriber;

        public ObservationTask(final Subscriber<? super T> subscriber) {
            this.subscriber = Preconditions.checkNotNull(subscriber);
        }

        @Override
        public void run() {
            try {
                final T response = provider.call();
                this.subscriber.onNext(response);
                this.subscriber.onCompleted();
            } catch (final Throwable rex) {
                this.subscriber.onError(rex);
            }
        }
    }

}
