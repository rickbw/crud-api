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

import crud.core.DataSource;
import crud.core.ReadableDataSet;
import crud.core.ReadableDataSet.Id;
import crud.core.Session;


/**
 * @see ReadableDataSet
 *
 * @author Rick Warren
 */
public class SyncReadableDataSet<K, E> extends SyncDelegateHolder<ReadableDataSet<K, E>> {

    public SyncReadableDataSet(@Nonnull final ReadableDataSet<K, E> delegate) {
        super(delegate);
    }

    /**
     * @see ReadableDataSet#getId()
     */
    public @Nonnull Id<K, E> getId() {
        return getDelegate().getId();
    }

    /**
     * @see ReadableDataSet#dataSource(Object, Session)
     */
    public @Nonnull SyncDataSource<E> dataSource(@Nonnull final K key, @Nonnull final SyncSession session) {
        final DataSource<E> delegateSource = getDelegate().dataSource(key, session.getDelegate());
        return new SyncDataSource<>(delegateSource);
    }

}
