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
package crud.implementer;

import java.util.Objects;

import javax.annotation.Nonnull;

import crud.core.Session;
import crud.core.TransactedSession;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;


/**
 * Schedules asynchronous {@link #commit()} and {@link #rollback()} operations
 * on behalf of a {@link TransactedSession} implementation. This functionality
 * is separable from {@link AbstractTransactedSession} for the benefit of
 * {@link Session} implementations that may need to share state and/or
 * business logic across transacted and non-transacted Sessions classes.
 * They can use inheritance along the business type hierarchy, and composition
 * to bring in commit/rollback scheduling.
 *
 * @author Rick Warren
 */
public abstract class TransactionLifecycle {

    private @Nonnull final SessionWorker worker;

    private final Task<Void> commitTask = new Task<Void>() {
        @Override
        public void call(final Subscriber<? super Void> sub) throws Exception {
            doCommit();
        }
    };
    private final Task<Void> rollbackTask = new Task<Void>() {
        @Override
        public void call(final Subscriber<? super Void> sub) throws Exception {
            doRollback();
        }
    };


    public TransactionLifecycle(@Nonnull final SessionWorker worker) {
        this.worker = Objects.requireNonNull(worker);
    }

    /**
     * An enclosing {@link TransactedSession} implementation (such as
     * {@link AbstractTransactedSession}) should delegate its own
     * {@link TransactedSession#commit()} implementation to this method.
     */
    public final Observable<Void> commit() {
        return this.worker.scheduleHot(this.commitTask);
    }

    /**
     * An enclosing {@link TransactedSession} implementation (such as
     * {@link AbstractTransactedSession}) should delegate its own
     * {@link TransactedSession#rollback()} implementation to this method.
     */
    public final Observable<Void> rollback() {
        return this.worker.scheduleHot(this.rollbackTask);
    }

    /**
     * Subclasses perform {@link #commit()} logic in here.
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected abstract void doCommit() throws Exception;

    /**
     * Subclasses perform {@link #rollback()} logic in here.
     *
     * @throws Exception    Subclasses may throw whatever they wish.
     *                      Exceptions will be passed to
     *                      {@link Observer#onError(Throwable)}.
     */
    protected abstract void doRollback() throws Exception;

}
