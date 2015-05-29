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
package crud.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import crud.core.Session;
import crud.core.TransactedSession;
import crud.implementer.SessionWorker;
import rx.Observable;
import rx.Subscriber;


/*package*/ final class JdbcTransactedSession extends JdbcSession implements TransactedSession {

    private final SessionWorker.Task<Void> commitTask = new SessionWorker.Task<Void>() {
        @Override
        public void call(final Subscriber<? super Void> sub) throws SQLException {
            getConnection().commit();
        }
    };
    private final SessionWorker.Task<Void> rollbackTask = new SessionWorker.Task<Void>() {
        @Override
        public void call(final Subscriber<? super Void> sub) throws SQLException {
            getConnection().rollback();
        }
    };


    public JdbcTransactedSession(@Nonnull final Connection connection) {
        super(connection);
    }

    @Override
    public Session.Ordering getOrdering() {
        return Session.Ordering.TRANSACTED;
    }

    @Override
    public Observable<Void> commit() {
        return getWorker().scheduleHot(this.commitTask);
    }

    @Override
    public Observable<Void> rollback() {
        return getWorker().scheduleHot(this.rollbackTask);
    }

}
