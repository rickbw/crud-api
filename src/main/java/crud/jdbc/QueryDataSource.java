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
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import crud.core.DataSource;
import crud.implementer.SessionWorker;
import rx.Observable;
import rx.Subscriber;


/*package*/ final class QueryDataSource extends StatementProvider implements DataSource<ResultSetRow> {

    public QueryDataSource(
            @Nonnull final Connection connection,
            @Nonnull final StatementTemplate statementTemplate,
            @Nonnull final SessionWorker worker) {
        super(connection, statementTemplate, worker);
    }

    @Override
    public Observable<ResultSetRow> read() {
        return Observable.create(new Observable.OnSubscribe<ResultSetRow>() {
            @Override
            public void call(final Subscriber<? super ResultSetRow> sub) {
                submit(new Callable<Void>() {
                    @SuppressWarnings("resource")
                    @Override
                    public Void call() throws Exception {
                        final PreparedStatement queryStmt = getStatement();
                        try (ResultSet results = queryStmt.executeQuery()) {
                            while (results.next()) {
                                final ResultSetRow currentRow = new ResultSetRow(results);
                                sub.onNext(currentRow);
                            }
                            // Don't call onCompleted(): that's called by submit()
                            //sub.onCompleted();
                        }
                        return null;
                    }
                });
            }
        });
    }

}
