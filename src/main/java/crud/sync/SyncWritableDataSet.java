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
import crud.core.Session;
import crud.core.WritableDataSet;
import crud.core.WritableDataSet.Id;


/**
 * @see WritableDataSet
 *
 * @author Rick Warren
 */
public class SyncWritableDataSet<K, E, R> extends SyncDelegateHolder<WritableDataSet<K, E, R>> {

    public SyncWritableDataSet(@Nonnull final WritableDataSet<K, E, R> delegate) {
        super(delegate);
    }

    /**
     * @see WritableDataSet#getId()
     */
    public @Nonnull Id<K, E, R> getId() {
        return getDelegate().getId();
    }

    /**
     * @see WritableDataSet#dataSink(Object, Session)
     */
    public @Nonnull SyncDataSink<E, R> dataSink(@Nonnull final K key, @Nonnull final SyncSession session) {
        final DataSink<E, R> delegateSink = getDelegate().dataSink(key, session.getDelegate());
        return new SyncDataSink<>(delegateSink);
    }

}
