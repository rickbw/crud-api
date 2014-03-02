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

package rickbw.crud.util.rx;

import rx.Observable;
import rx.Subscription;


/**
 * A {@link Subscription} that cannot be unsubscribed from. This
 * implementation is appropriate for synchronous {@link Observable}s, which
 * have already completed by the time the caller receives the Subscription.
 */
public final class NoOpSubscription implements Subscription {

    private static final Subscription singleton = new NoOpSubscription();


    public static Subscription getInstance() {
        return singleton;
    }

    @Override
    public void unsubscribe() {
        // do nothing
    }

    private NoOpSubscription() {
        // prevent external instantiation
    }

}
