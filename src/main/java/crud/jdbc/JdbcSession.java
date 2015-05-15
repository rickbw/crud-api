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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import crud.core.MiddlewareException;
import crud.core.Session;
import crud.util.SessionWorker;
import rx.Observable;


public class JdbcSession implements Session {

    private final SessionWorker worker = new SessionWorker();
    private @Nonnull final Connection connection;

    private final Callable<Void> commitTask = new Callable<Void>() {
        @Override
        public Void call() throws SQLException {
            JdbcSession.this.connection.commit();
            return null;
        }
    };
    private final Callable<Void> rollbackTask = new Callable<Void>() {
        @Override
        public Void call() throws SQLException {
            JdbcSession.this.connection.rollback();
            return null;
        }
    };
    private final Callable<Void> closeTask = new Callable<Void>() {
        @Override
        public Void call() throws SQLException {
            JdbcSession.this.connection.close();
            return null;
        }
    };


    public JdbcSession(
            @Nonnull final Connection connection,
            @Nonnull final Session.Ordering ordering) {
        this.connection = connection;

        try {
            final boolean isTransactional = (ordering == Session.Ordering.TRANSACTIONAL);
            this.connection.setAutoCommit(!isTransactional);
        } catch (final SQLException sx) {
            throw new MiddlewareException(sx.getMessage(), sx);
        }
    }

    @Override
    public Session.Ordering getOrdering() {
        try {
            final boolean isAutoCommit = this.connection.getAutoCommit();
            return isAutoCommit ? Session.Ordering.ORDERED : Session.Ordering.TRANSACTIONAL;
        } catch (final SQLException sx) {
            throw new MiddlewareException(sx.getMessage(), sx);
        }
    }

    @Override
    public Observable<Void> commit() {
        return this.worker.submit(this.commitTask);
    }

    @Override
    public Observable<Void> rollback() {
        return this.worker.submit(this.rollbackTask);
    }

    @Override
    public Observable<Void> stop() {
        return this.worker.stop(this.closeTask, Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

}
