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
import java.util.Objects;

import crud.core.DataSet;
import crud.core.DataSetId;
import crud.core.DataSource;
import crud.core.Session;
import crud.util.SessionWorker;


/*package*/ class Table implements DataSet<StatementTemplate, ResultSetRow> {

    private final DataSetId<StatementTemplate, ResultSetRow> id;


    public Table(final DataSetId<StatementTemplate, ResultSetRow> id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public DataSetId<StatementTemplate, ResultSetRow> getId() {
        return this.id;
    }

    @SuppressWarnings("resource")
    @Override
    public DataSource<ResultSetRow> dataSource(final Session session, final StatementTemplate query) {
        final JdbcSession jdbcSession = (JdbcSession) session;
        final Connection connection = jdbcSession.getConnection();
        final SessionWorker worker = jdbcSession.getWorker();
        return new QueryDataSource(connection, query, worker);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + this.id + ')';
    }

}
