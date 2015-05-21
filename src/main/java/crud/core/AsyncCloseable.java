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
package crud.core;

import rx.Observable;
import rx.Observer;


/**
 * An analog to {@link AutoCloseable} in which shutdown occurs asynchronously,
 * presumably because it would otherwise requires blocking.
 * <p/>
 * <b>Design Rationale</b>: Why not just use {@link AutoCloseable}? Because
 * that would encourage block-structured code, such as with try-with-resources,
 * and asynchronous data processing is not naturally block structured.
 * Applications would tend to inadvertently shut down their
 * {@link DataBus} underneath their asynchronously-running I/O operations.
 *
 * @author Rick Warren
 */
public interface AsyncCloseable {

    /**
     * Initiate the close of this {@link AsyncCloseable}. Once this DataBus is
     * shut down, it will no longer be possible to user for other purposes;
     * this state cannot be undone.
     * <p/>
     * The shutdown commences immediately with the call to this method; it
     * does not require the resulting {@link Observable} to be subscribed.
     * That Observable behaves as if {@link Observable#cache() cached}: the
     * same result will be emitted to any subscriber.
     *
     * @return  A zero-element {@link Observable}. It will report
     *          {@link Observer#onCompleted() onCompleted} on successful
     *          shutdown, or {@link Observer#onError(Throwable) onError} if
     *          an error occurred.
     */
    public Observable<Void> shutdown();

}
