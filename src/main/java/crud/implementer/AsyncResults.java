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
import java.util.List;

import crud.core.AsyncCloseable;
import rx.Notification;
import rx.Observable;


/**
 * Utility methods pertaining to {@link Observable Observable<Void>} and
 * {@link AsyncCloseable}.
 *
 * @author Rick Warren
 */
public final class AsyncResults {

    public static Observable<Void> shutdownAll(final Iterable<? extends AsyncCloseable> shutUsDown) {
        final List<Observable<Void>> results = new ArrayList<>();
        for (final AsyncCloseable shutMeDown : shutUsDown) {
            /* Per the contract of shutdown(), these result Observables are
             * hot. We don't need to subscribe to them here to make the
             * shutdowns happen.
             */
            results.add(shutMeDown.shutdown());
        }

        /* Per our conventions, no Observable<Void> should ever emit any
         * elements, so concat'ing them together should yield simply an
         * onCompleted() (if all complete successfully) or an onError
         * (carrying the first failure that was seen). However, just in case
         * someone does try to emit any data elements, call ignoreElements()
         * to eliminate them.
         */
        return Observable.concat(Observable.from(results)).ignoreElements();
    }

    public static void awaitShutdown(final AsyncCloseable shutMeDown) throws Exception {
        final Observable<Void> shutdownResult = shutMeDown.shutdown();
        awaitCompletion(shutdownResult);
    }

    public static void awaitCompletion(final Observable<Void> obs) throws Exception {
        // Will be onCompleted or onError:
        final Notification<Void> last = obs.materialize().toBlocking().last();
        if (last.isOnError()) {
            assert last.hasThrowable();
            final Throwable ex = last.getThrowable();
            if (ex instanceof Error) {
                throw (Error) ex;
            } else {
                /* Extending or throwing Throwable is bad. Assume no one would
                 * do such a horrible thing.
                 */
                assert ex instanceof Exception;
                throw (Exception) ex;
            }
        }
    }

    private AsyncResults() {
        // prevent instantiation
    }

}
