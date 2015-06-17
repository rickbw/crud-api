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

import javax.annotation.Nonnull;

import crud.core.Session;
import crud.core.TransactedSession;
import rx.Observable;
import rx.Observer;


public abstract class AbstractTransactedSession extends AbstractSession implements TransactedSession {

    private @Nonnull final TransactionLifecycle tx;


    /**
     * Subclasses should prefer to override {@link #doCommit()} instead of
     * this method.
     */
    @Override
    public final Observable<Void> commit() {
        return this.tx.commit();
    }

    /**
     * Subclasses should prefer to override {@link #doRollback()} instead of
     * this method.
     */
    @Override
    public final Observable<Void> rollback() {
        return this.tx.rollback();
    }

    protected AbstractTransactedSession(@Nonnull final SessionWorker worker) {
        super(worker, Session.Ordering.TRANSACTED);

        this.tx = new TransactionLifecycle(worker) {
            @Override
            protected void doCommit() throws Exception {
                AbstractTransactedSession.this.doCommit();
            }

            @Override
            protected void doRollback() throws Exception {
                AbstractTransactedSession.this.doRollback();
            }
        };
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
