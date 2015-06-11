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

import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import com.google.common.base.Optional;

import crud.core.DataBus;
import crud.core.ReadableResourceSet;
import crud.core.Session;
import crud.core.TransactedSession;
import crud.core.WritableResourceSet;
import crud.implementer.AsyncResults;


/**
 * @see DataBus
 *
 * @author Rick Warren
 */
@ThreadSafe
public class SyncDataBus extends SyncDelegateHolder<DataBus> implements AutoCloseable {

    public SyncDataBus(@Nonnull final DataBus delegate) {
        super(delegate);
    }

    /**
     * @see DataBus#start()
     */
    public void start() {
        getDelegate().start();
    }

    /**
     * @see DataBus#resources(crud.core.ReadableResourceSet.Id)
     */
    public <K, E> Optional<SyncReadableResourceSet<K, E>> resources(final ReadableResourceSet.Id<K, E> id) {
        final Optional<ReadableResourceSet<K, E>> optResources = getDelegate().resources(id);
        if (optResources.isPresent()) {
            return Optional.of(new SyncReadableResourceSet<>(optResources.get()));
        } else {
            return Optional.absent();
        }
    }

    /**
     * @see DataBus#resources(crud.core.WritableResourceSet.Id)
     */
    public <K, E, R> Optional<SyncWritableResourceSet<K, E, R>> resources(final WritableResourceSet.Id<K, E, R> id) {
        final Optional<WritableResourceSet<K, E, R>> optResources = getDelegate().resources(id);
        if (optResources.isPresent()) {
            return Optional.of(new SyncWritableResourceSet<>(optResources.get()));
        } else {
            return Optional.absent();
        }
    }

    /**
     * @see DataBus#getSupportedSessionOrderings()
     */
    public Set<Session.Ordering> getSupportedSessionOrderings() {
        return getDelegate().getSupportedSessionOrderings();
    }

    /**
     * @see DataBus#startSession(boolean)
     */
    public @Nonnull SyncSession startSession(final boolean requireOrdering) {
        final Session delegateSession = getDelegate().startSession(requireOrdering);
        return new SyncSession(delegateSession);
    }

    /**
     * @see DataBus#startTransactedSession()
     */
    public @Nonnull SyncTransactedSession startTransactedSession() {
        final TransactedSession delegateSession = getDelegate().startTransactedSession();
        return new SyncTransactedSession(delegateSession);
    }

    /**
     * @see DataBus#shutdown()
     */
    @Override
    public void close() throws Exception {
        AsyncResults.awaitShutdown(getDelegate());
    }

}
