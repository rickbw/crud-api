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

import java.util.Objects;

import rx.Observable;
import rx.Observer;
import rx.Subscription;


/*package*/ final class PendingSubscription<T> {

    private final Observable<? extends T> observable;
    private final Observer<? super T> observer;


    public PendingSubscription(
            final Observable<? extends T> observable,
            final Observer<? super T> observer) {
        this.observable = Objects.requireNonNull(observable);
        this.observer = Objects.requireNonNull(observer);
    }

    public void run() {
        // TODO: Subscriber on some Scheduler?
        final Subscription subscription = this.observable.subscribe(this.observer);
        // TODO: What to do with the Subscription?
    }

}
