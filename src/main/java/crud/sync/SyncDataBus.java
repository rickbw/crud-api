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
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Optional;

import crud.core.DataBus;
import crud.core.ReadableDataSet;
import crud.core.Session;
import crud.core.TransactedSession;
import crud.core.WritableDataSet;
import crud.implementer.AsyncResults;


/**
 * @see DataBus
 *
 * @author Rick Warren
 */
@ThreadSafe
public class SyncDataBus implements AutoCloseable {

    private @Nonnull final DataBus delegate;


    public SyncDataBus(@Nonnull final DataBus delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    /**
     * @see DataBus#start()
     */
    public void start() {
        this.delegate.start();
    }

    /**
     * @see DataBus#dataSet(crud.core.ReadableDataSet.Id)
     */
    public <K, E> Optional<SyncReadableDataSet<K, E>> dataSet(final ReadableDataSet.Id<K, E> id) {
        final Optional<ReadableDataSet<K, E>> optDataSet = this.delegate.dataSet(id);
        if (optDataSet.isPresent()) {
            return Optional.of(new SyncReadableDataSet<>(optDataSet.get()));
        } else {
            return Optional.absent();
        }
    }

    /**
     * @see DataBus#dataSet(crud.core.WritableDataSet.Id)
     */
    public <K, E, R> Optional<SyncWritableDataSet<K, E, R>> dataSet(final WritableDataSet.Id<K, E, R> id) {
        final Optional<WritableDataSet<K, E, R>> optDataSet = this.delegate.dataSet(id);
        if (optDataSet.isPresent()) {
            return Optional.of(new SyncWritableDataSet<>(optDataSet.get()));
        } else {
            return Optional.absent();
        }
    }

    /**
     * @see DataBus#getSupportedSessionOrderings()
     */
    public Set<Session.Ordering> getSupportedSessionOrderings() {
        return this.delegate.getSupportedSessionOrderings();
    }

    /**
     * @see DataBus#startSession(boolean)
     */
    public @Nonnull SyncSession startSession(final boolean requireOrdering) {
        final Session delegateSession = this.delegate.startSession(requireOrdering);
        return new SyncSession(delegateSession);
    }

    /**
     * @see DataBus#startTransactedSession()
     */
    public @Nonnull SyncTransactedSession startTransactedSession() {
        final TransactedSession delegateSession = this.delegate.startTransactedSession();
        return new SyncTransactedSession(delegateSession);
    }

    /**
     * @see DataBus#shutdown()
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
        final SyncDataBus other = (SyncDataBus) obj;
        return this.delegate.equals(other.delegate);
    }

    @Override
    public int hashCode() {
        return 31 + this.delegate.hashCode();
    }

}
