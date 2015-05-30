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

import java.util.Objects;

import javax.annotation.Nonnull;

import crud.core.DataSink;
import crud.implementer.AsyncResults;


/**
 * @see DataSink
 *
 * @author Rick Warren
 */
public class SyncDataSink<E, R> implements AutoCloseable {

    private @Nonnull final DataSink<E, R> delegate;


    public SyncDataSink(@Nonnull final DataSink<E, R> delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    /**
     * @see DataSink#write(Object)
     */
    public Iterable<R> write(final E value) {
        return this.delegate.write(value).toBlocking().toIterable();
    }

    /**
     * @see DataSink#shutdown()
     */
    @Override
    public void close() throws Exception {
        AsyncResults.awaitShutdown(this.delegate);
    }

    @Override
    public String toString() {
        return "Sync(" + this.delegate + ')';
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SyncDataSink<?, ?> other = (SyncDataSink<?, ?>) obj;
        return this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return 31 + this.delegate.hashCode();
    }

}