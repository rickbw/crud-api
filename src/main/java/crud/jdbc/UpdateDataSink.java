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
import java.sql.SQLException;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import crud.core.DataSink;
import crud.implementer.SessionWorker;
import rx.Observable;
import rx.Subscriber;


/*package*/ final class UpdateDataSink
extends StatementProvider
implements DataSink<StatementParameters, Integer> {

    public UpdateDataSink(
            @Nonnull final Connection connection,
            @Nonnull final StatementTemplate statementTemplate,
            @Nonnull final SessionWorker worker) {
        super(connection, statementTemplate, worker);
    }

    @Override
    public Observable<Integer> write(final StatementParameters params) {
        final Observable<Integer> result = Observable.create(new StatementExecutor(params)).cache();
        /* Start a subscription now, so that the executeUpdate() occurs
         * immediately. The no-argument subscribe() does not handle errors, so
         * materialize() to prevent it from seeing any.
         */
        result.materialize().subscribe();
        return result;
    }


    private final class StatementExecutor implements Observable.OnSubscribe<Integer> {
        private final StatementParameters params;

        public StatementExecutor(final StatementParameters params) {
            this.params = params;
        }

        @Override
        public void call(final Subscriber<? super Integer> sub) {
            submit(new Callable<Void>() {
                @SuppressWarnings("resource")
                @Override
                public Void call() throws SQLException {
                    final PreparedStatement updateStmt = getStatement();
                    StatementExecutor.this.params.substituteAll(updateStmt);
                    final int nRowsUpdated = updateStmt.executeUpdate();
                    sub.onNext(nRowsUpdated);
                    // Don't call onCompleted(): that's called by submit()
                    //sub.onCompleted();
                    return null;
                }
            });
        }
    }

}
