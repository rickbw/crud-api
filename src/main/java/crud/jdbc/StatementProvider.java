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
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import crud.implementer.AbstractSessionParticipant;
import crud.implementer.SessionWorker;


/*package*/ class StatementProvider extends AbstractSessionParticipant {

    private @Nonnull final Connection connection;
    private @Nonnull final StatementTemplate statementTemplate;

    /**
     * A query to be executed, cached for reuse. Should only be accessed in
     * the {@link #worker}'s thread, so no need to make it volatile or worry
     * about race conditions.
     */
    private @Nullable PreparedStatement statement = null;


    protected StatementProvider(
            @Nonnull final Connection connection,
            @Nonnull final StatementTemplate statementTemplate,
            @Nonnull final SessionWorker worker) {
        super(worker);
        this.connection = Objects.requireNonNull(connection);
        this.statementTemplate = Objects.requireNonNull(statementTemplate);
    }

    protected final @Nonnull PreparedStatement getStatement() throws SQLException {
        PreparedStatement raceFreeStmt = this.statement;
        if (raceFreeStmt == null) {
            raceFreeStmt = this.statementTemplate.prepareStatement(this.connection);
            this.statement = raceFreeStmt;
        }
        assert this.statement != null;
        return raceFreeStmt;
    }

    @Override
    protected void doShutdown() throws SQLException {
        final PreparedStatement raceFreeStmt = StatementProvider.this.statement;
        if (raceFreeStmt != null) {
            raceFreeStmt.close();
        }
    }

}
