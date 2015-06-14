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
package crud.jms;

import crud.core.Session;
import crud.core.TransactedSession;
import crud.implementer.TransactionLifecycle;
import rx.Observable;


/*package*/ final class TransactedJmsSession extends SessionWrapper implements TransactedSession {

    private final TransactionLifecycle tx;


    public TransactedJmsSession(final javax.jms.Session delegate) {
        super(Session.Ordering.TRANSACTED, delegate);
        // Assumed, but illegal to check in this thread:
        //assert getDelegate().getTransacted();
        this.tx = new TransactionLifecycle(getWorker()) {
            @Override
            protected void doCommit() throws Exception {
                getDelegate().commit();
            }

            @Override
            protected void doRollback() throws Exception {
                getDelegate().rollback();
            }
        };
    }

    @Override
    public Observable<Void> commit() {
        return this.tx.commit();
    }

    @Override
    public Observable<Void> rollback() {
        return this.tx.rollback();
    }

}
