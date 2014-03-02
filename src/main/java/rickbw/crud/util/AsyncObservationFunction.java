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

import rickbw.crud.util.rx.FutureSubscription;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.util.functions.Func1;


/**
 * Dispatches an object returned from a {@link Callable} to {@link Observer}s
 * asynchronously using an {@link ExecutorService}. This class is designed to
 * eliminate boilerplate code when working with asynchronous
 * {@link Observable} implementations.
 */
public final class AsyncObservationFunction<T> implements Func1<Observer<T>, Subscription> {

    private final Callable<T> provider;
    private final ExecutorService executor;


    /**
     * The given data provider will be executed on the given executor service
     * each time that {@link #call(Observer)} is invoked.
     */
    public AsyncObservationFunction(final Callable<T> provider, final ExecutorService executor) {
        this.provider = Preconditions.checkNotNull(provider);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public Subscription call(final Observer<T> observer) {
        final Runnable task = new ObservationTask(observer);
        final Future<?> taskResult = this.executor.submit(task);
        final Subscription subscription = new FutureSubscription(taskResult);
        return subscription;
    }

    private final class ObservationTask implements Runnable {
        private final Observer<T> observer;

        public ObservationTask(final Observer<T> observer) {
            this.observer = Preconditions.checkNotNull(observer);
        }

        @Override
        public void run() {
            try {
                final T response = provider.call();
                this.observer.onNext(response);
                this.observer.onCompleted();
            } catch (final Exception rex) {
                this.observer.onError(rex);
            }
        }
    }

}
