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

import javax.annotation.Nonnull;

import crud.core.ReadableResource;
import crud.core.Session;
import crud.core.WritableResource;
import crud.implementer.AbstractSession;
import crud.implementer.SessionWorker;


/*package*/ class JdbcSession extends AbstractSession {

    private @Nonnull final Connection connection;


    public JdbcSession(@Nonnull final Connection connection) {
        super(SessionWorker.create(), Session.Ordering.ORDERED);
        this.connection = Objects.requireNonNull(connection);
    }

    protected final @Nonnull Connection getConnection() {
        return this.connection;
    }

    @Override
    protected void doShutdown() throws SQLException {
        this.connection.close();
    }

    /*package*/ final @Nonnull ReadableResource<ResultSetRow> readableResource(final StatementTemplate query) {
        return new QueryResource(this.connection, query, getWorker());
    }

    /*package*/ final @Nonnull WritableResource<StatementParameters, Integer> writableResource(final StatementTemplate update) {
        return new UpdateResource(this.connection, update, getWorker());
    }

}
