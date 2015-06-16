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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.annotation.Nonnull;

import crud.core.ReadableResource;
import crud.implementer.SessionWorker;
import crud.implementer.Task;
import rx.Observable;
import rx.Subscriber;


/*package*/ final class QueryResource
extends StatementProvider<ResultSetRow>
implements ReadableResource<ResultSetRow> {

    public QueryResource(
            @Nonnull final Connection connection,
            @Nonnull final StatementTemplate statementTemplate,
            @Nonnull final SessionWorker worker) {
        super(connection, statementTemplate, worker);
    }

    @Override
    public Observable<ResultSetRow> read() {
        return getWorker().scheduleCold(new Task<ResultSetRow>() {
            @Override
            public void call(final Subscriber<? super ResultSetRow> sub) throws SQLException {
                @SuppressWarnings("resource")
                final PreparedStatement queryStmt = getStatement();
                try (ResultSet results = queryStmt.executeQuery()) {
                    while (results.next()) {
                        @SuppressWarnings("resource")
                        final ResultSetRow currentRow = new ResultSetRow(results);
                        sub.onNext(currentRow);
                    }
                }
            }
        });
    }

}
