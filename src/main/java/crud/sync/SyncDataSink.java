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
package crud.sync;

import javax.annotation.Nonnull;

import crud.core.DataSink;
import crud.implementer.AsyncResults;


/**
 * @see DataSink
 *
 * @author Rick Warren
 */
public class SyncDataSink<E, R> extends SyncDelegateHolder<DataSink<E, R>> implements AutoCloseable {

    public SyncDataSink(@Nonnull final DataSink<E, R> delegate) {
        super(delegate);
    }

    /**
     * @see DataSink#write(Object)
     */
    public Iterable<R> write(final E value) {
        return getDelegate().write(value).toBlocking().toIterable();
    }

    /**
     * @see DataSink#shutdown()
     */
    @Override
    public void close() throws Exception {
        AsyncResults.awaitShutdown(getDelegate());
    }

}
