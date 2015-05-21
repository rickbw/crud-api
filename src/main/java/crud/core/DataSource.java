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
 * A <em>source</em> of data elements of type {@code E}, read by the
 * application from somewhere else.
 *
 * @param <E>   The static type of the data elements observable from this
 *              {@link DataSource}.
 *
 * @see DataSink
 *
 * @author Rick Warren
 */
public interface DataSource<E> extends AsyncCloseable {

    /**
     * Begin observation of the data elements. The {@link Observable} may be
     * either "hot" or "cold", depending on the nature of the data source.
     * <p/>
     * <b>Note to implementers</b>: Except in special cases, consuming from
     * this {@link DataSource} will involve I/O. In the interest of
     * segregating those threads that perform blocking I/O and those that
     * perform computation, implementations should not block on this call, nor
     * should elements be delivered in the thread
     * {@link Observable#subscribe() subscribing} to the Observable. Instead,
     * data elements should be delivered to {@link Observer}s in a thread
     * associated with the {@link Session} used to create this
     * {@code DataSource}.
     */
    public Observable<E> read();

}
