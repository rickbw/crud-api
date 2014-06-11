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
package rickbw.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Assert;

import com.google.common.collect.Lists;

import rx.Observable;
import rx.Observer;


/**
 * A set of test assertions, similar to {@link Assert}, that operate on RxJava
 * types.
 */
public final class RxAssertions {

    public static void assertObservablesEqual(
            final Observable<?> expected,
            final Observable<?> actual) {
        // Observables should never be null:
        assertNotNull(expected);
        assertNotNull(actual);

        // materialize() to collapse exceptions and values together
        final List<?> expectedValues = Lists.newArrayList(expected
                .materialize()
                .toBlocking()
                .toIterable());
        final List<?> actualValues = Lists.newArrayList(actual
                .materialize()
                .toBlocking()
                .toIterable());
        assertEquals(expectedValues, actualValues);
    }

    public static <T> void subscribeWithOnNextFailure(final Observable<T> obs)
    throws InterruptedException {
        // given:
        final String success = "success";
        final String failed = "failed";
        final AtomicReference<String> onNextCalled = new AtomicReference<>(failed);
        final AtomicReference<String> onCompleteNotCalled = new AtomicReference<>(success);
        final AtomicReference<String> onErrorCalled = new AtomicReference<>(failed);

        subscribeAndWait(obs, 2, new Observer<T>() {
            @Override
            public void onNext(final T t) {
                onNextCalled.set(success);
                throw new IllegalStateException("mock exception");
            }

            @Override
            public void onCompleted() {
                onCompleteNotCalled.set(failed);
            }

            @Override
            public void onError(final Throwable e) {
                onErrorCalled.set(success);
            }
        });

        // then:
        assertEquals(success, onNextCalled.get());
        assertEquals(success, onCompleteNotCalled.get());
        assertEquals(success, onErrorCalled.get());
    }

    public static <T> void subscribeWithOnCompletedFailure(final Observable<T> obs)
    throws InterruptedException {
        // given:
        final String success = "success";
        final String failed = "failed";
        final AtomicReference<String> onNextCalled = new AtomicReference<>(failed);
        final AtomicReference<String> onCompleteCalled = new AtomicReference<>(failed);
        final AtomicReference<String> onErrorCalled = new AtomicReference<>(failed);

        subscribeAndWait(obs, 3, new Observer<T>() {
            @Override
            public void onNext(final T t) {
                onNextCalled.set(success);
            }

            @Override
            public void onCompleted() {
                onCompleteCalled.set(success);
                throw new IllegalStateException("mock exception");
            }

            @Override
            public void onError(final Throwable e) {
                onErrorCalled.set(success);
            }
        });

        // then:
        assertEquals(success, onNextCalled.get());
        assertEquals(success, onCompleteCalled.get());
        assertEquals(success, onErrorCalled.get());
    }

    public static <T> void subscribeWithOnNextAndOnErrorFailures(final Observable<T> obs)
    throws InterruptedException {
        // given:
        final String success = "success";
        final String failed = "failed";
        final AtomicReference<String> onNextCalled = new AtomicReference<>(failed);
        final AtomicReference<String> onCompleteNotCalled = new AtomicReference<>(success);
        final AtomicReference<String> onErrorCalled = new AtomicReference<>(failed);

        subscribeAndWait(obs, 2, new Observer<T>() {
            @Override
            public void onNext(final T t) {
                onNextCalled.set(success);
                throw new IllegalStateException("trigger onError");
            }

            @Override
            public void onCompleted() {
                onCompleteNotCalled.set(failed);
            }

            @Override
            public void onError(final Throwable e) {
                onErrorCalled.set(success);
                throw new IllegalStateException("mock exception");
            }
        });

        // then:
        assertEquals(success, onNextCalled.get());
        assertEquals(success, onCompleteNotCalled.get());
        assertEquals(success, onErrorCalled.get());
    }

    public static <T> void subscribeWithOnCompletedAndOnErrorFailures(final Observable<T> obs)
    throws InterruptedException {
        // given:
        final String success = "success";
        final String failed = "failed";
        final AtomicReference<String> onNextCalled = new AtomicReference<>(failed);
        final AtomicReference<String> onCompleteCalled = new AtomicReference<>(failed);
        final AtomicReference<String> onErrorCalled = new AtomicReference<>(failed);

        subscribeAndWait(obs, 3, new Observer<T>() {
            @Override
            public void onNext(final T t) {
                onNextCalled.set(success);
            }

            @Override
            public void onCompleted() {
                onCompleteCalled.set(success);
                throw new IllegalStateException("trigger onError");
            }

            @Override
            public void onError(final Throwable e) {
                onErrorCalled.set(success);
                throw new IllegalStateException("mock exception");
            }
        });

        // then:
        assertEquals(success, onNextCalled.get());
        assertEquals(success, onCompleteCalled.get());
        assertEquals(success, onErrorCalled.get());
    }

    public static <T> void subscribeAndWait(
            final Observable<T> observable,
            final int expectedObserverCalls,
            final Observer<T> observer) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(expectedObserverCalls);
        final Observer<T> wrapperObserver = new Observer<T>() {
            @Override
            public void onNext(final T t) {
                latch.countDown();
                observer.onNext(t);
            }

            @Override
            public void onCompleted() {
                latch.countDown();
                observer.onCompleted();
            }

            @Override
            public void onError(final Throwable e) {
                latch.countDown();
                observer.onError(e);
            }
        };
        observable.subscribe(wrapperObserver);
        latch.await();
    }

    private RxAssertions() {
        // prevent instantiation
    }

}
