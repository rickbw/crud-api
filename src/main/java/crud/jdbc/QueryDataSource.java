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
import java.util.Objects;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import crud.core.DataSource;
import crud.util.SessionWorker;
import rx.Observable;
import rx.Subscriber;


/*package*/ class QueryDataSource implements DataSource<ResultSetRow> {

    private @Nonnull final Connection connection;
    private @Nonnull final StatementTemplate statementTemplate;
    private @Nonnull final SessionWorker worker;

    /**
     * A query to be executed, cached for reuse. Should only be accessed in
     * the {@link #worker}'s thread, so no need to make it volatile or worry
     * about race conditions.
     */
    private @Nullable PreparedStatement statement = null;


    public QueryDataSource(
            @Nonnull final Connection connection,
            @Nonnull final StatementTemplate statementTemplate,
            @Nonnull final SessionWorker worker) {
        this.connection = Objects.requireNonNull(connection);
        this.statementTemplate = Objects.requireNonNull(statementTemplate);
        this.worker = Objects.requireNonNull(worker);
    }

    @Override
    public Observable<ResultSetRow> read() {
        return Observable.create(new Observable.OnSubscribe<ResultSetRow>() {
            @Override
            public void call(final Subscriber<? super ResultSetRow> sub) {
                QueryDataSource.this.worker.submit(new Callable<Void>() {
                    @SuppressWarnings("resource")
                    @Override
                    public Void call() throws Exception {
                        final PreparedStatement raceFreeStmt = getStatement();
                        try (ResultSet results = raceFreeStmt.executeQuery()) {
                            while (results.next()) {
                                final ResultSetRow currentRow = new ResultSetRow(results);
                                sub.onNext(currentRow);
                            }
                            sub.onCompleted();
                        }
                        return null;
                    }
                });
            }
        });
    }

    @Override
    public Observable<Void> shutdown() {
        return this.worker.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final PreparedStatement raceFreeStmt = QueryDataSource.this.statement;
                if (raceFreeStmt != null) {
                    raceFreeStmt.close();
                }
                return null;
            }
        });
    }

    private @Nonnull PreparedStatement getStatement() throws SQLException {
        PreparedStatement raceFreeStmt = QueryDataSource.this.statement;
        if (raceFreeStmt == null) {
            raceFreeStmt = this.statementTemplate.prepareStatement(this.connection);
            QueryDataSource.this.statement = raceFreeStmt;
        }
        assert this.statement != null;
        return raceFreeStmt;
    }

}
