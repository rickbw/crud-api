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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import crud.implementer.SessionWorker;

import crud.core.Session;
import rx.Observable;


/*package*/ class JdbcSession implements Session {

    private final SessionWorker worker = new SessionWorker();
    private @Nonnull final Connection connection;

    private final Callable<Void> closeTask = new Callable<Void>() {
        @Override
        public Void call() throws SQLException {
            JdbcSession.this.connection.close();
            return null;
        }
    };


    public JdbcSession(@Nonnull final Connection connection) {
        this.connection = Objects.requireNonNull(connection);
    }

    @Override
    public Session.Ordering getOrdering() {
        return Session.Ordering.ORDERED;
    }

    @Override
    public Observable<Void> shutdown() {
        return this.worker.shutdown(this.closeTask, Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    protected @Nonnull Connection getConnection() {
        return this.connection;
    }

    protected @Nonnull SessionWorker getWorker() {
        return this.worker;
    }

}
