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
import java.util.Queue;

import rx.Observable;
import rx.Subscriber;


/*package*/ class SessionAppender<T> implements Observable.OnSubscribe<T> {

    private final Observable<? extends T> delegate;
    private final Queue<? super PendingSubscription<T>> pendingSubscriptions;


    public SessionAppender(
            final Observable<? extends T> delegate,
            final Queue<? super PendingSubscription<T>> pendingSubscriptions) {
        this.delegate = Objects.requireNonNull(delegate);
        this.pendingSubscriptions = Objects.requireNonNull(pendingSubscriptions);
    }

    @Override
    public void call(final Subscriber<? super T> subscriber) {
        final PendingSubscription<T> sub = new PendingSubscription<>(this.delegate, subscriber);
        this.pendingSubscriptions.add(sub);
    }

}
