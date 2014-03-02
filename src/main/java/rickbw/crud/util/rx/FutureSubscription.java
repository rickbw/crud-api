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

import java.util.concurrent.Future;

import rickbw.crud.util.Preconditions;
import rx.Subscription;


/**
 * A {@link Subscription} that wraps a {@link Future}.
 */
public class FutureSubscription implements Subscription {
    private final Future<?> task;
    private final boolean mayInterruptIfRunning;


    public FutureSubscription(final Future<?> task) {
        this(task, false);
    }

    public FutureSubscription(final Future<?> task, final boolean mayInterruptIfRunning) {
        this.task = Preconditions.checkNotNull(task);
        this.mayInterruptIfRunning = mayInterruptIfRunning;
    }

    /**
     * Cancel the {@link Future}, as with {@link Future#cancel(boolean)}.
     * If the task represented by the future has already started (or even
     * finished), this operation will have no effect.
     */
    @Override
    public void unsubscribe() {
        this.task.cancel(this.mayInterruptIfRunning);
    }
}
