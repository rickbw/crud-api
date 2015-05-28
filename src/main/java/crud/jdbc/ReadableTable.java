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

import crud.core.DataSource;
import crud.core.ReadableDataSet;
import crud.core.Session;
import crud.implementer.AbstractReadableDataSet;
import crud.implementer.SessionWorker;


/*package*/ final class ReadableTable extends AbstractReadableDataSet<StatementTemplate, ResultSetRow> {

    public ReadableTable(final ReadableDataSet.Id<StatementTemplate, ResultSetRow> id) {
        super(id);
    }

    @SuppressWarnings("resource")
    @Override
    public DataSource<ResultSetRow> dataSource(final StatementTemplate query, final Session session) {
        final JdbcSession jdbcSession = (JdbcSession) session;
        final Connection connection = jdbcSession.getConnection();
        final SessionWorker worker = jdbcSession.worker();
        return new QueryDataSource(connection, query, worker);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + getId() + ')';
    }

}
