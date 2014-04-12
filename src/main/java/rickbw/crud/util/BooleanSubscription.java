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
package rickbw.crud.util;

import java.util.concurrent.atomic.AtomicBoolean;

import rx.Subscription;


/**
 * A {@link Subscription} based on a simple {@link AtomicBoolean} flag. The
 * creator of this Subscription retains a reference to the flag and
 * periodically checks it for unsubscription.
 */
public final class BooleanSubscription implements Subscription {

    private final AtomicBoolean unsubscribed;


    public BooleanSubscription(final AtomicBoolean unsubscribed) {
        this.unsubscribed = Preconditions.checkNotNull(unsubscribed);
    }


    @Override
    public void unsubscribe() {
        this.unsubscribed.set(true);
    }

    @Override
    public boolean isUnsubscribed() {
        return this.unsubscribed.get();
    }

}
