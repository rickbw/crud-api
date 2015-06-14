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
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import crud.core.ReadableResourceSet;
import crud.core.Session;
import crud.core.TransactedSession;
import crud.core.WritableResourceSet;
import crud.implementer.AbstractDataBus;


public class JdbcDataBus extends AbstractDataBus {

    private static final Logger log = LoggerFactory.getLogger(JdbcDataBus.class);

    private static final Set<Session.Ordering> supportedOrderings = Collections.unmodifiableSet(
            EnumSet.allOf(Session.Ordering.class));

    private @Nonnull final DataSource dataSource;
    private final Optional<String> username;
    private final Optional<String> password;


    public JdbcDataBus(@Nonnull final DataSource dataSource) {
        this(dataSource, Optional.<String>absent(), Optional.<String>absent());
    }

    public JdbcDataBus(
            @Nonnull final DataSource dataSource,
            final String username,
            final String password) {
        this(dataSource, Optional.of(username), Optional.of(password));
    }

    private JdbcDataBus(
            @Nonnull final DataSource dataSource,
            final Optional<String> username,
            final Optional<String> password) {
        this.dataSource = Objects.requireNonNull(dataSource);
        this.username = Objects.requireNonNull(username);
        this.password = Objects.requireNonNull(password);
        Preconditions.checkArgument(
                this.username.isPresent() == this.password.isPresent(),
                "Either both username and password must be present, or neither");
    }

    @Override
    public Set<Session.Ordering> getSupportedSessionOrderings() {
        return supportedOrderings;
    }

    @Override
    @SuppressWarnings("resource")
    protected Session doStartOrderedSession() throws SQLException {
        final Connection connection = getConnection();
        connection.setAutoCommit(true);
        return new JdbcSession(connection);
    }

    @Override
    @SuppressWarnings("resource")
    protected TransactedSession doStartTransactedSession() throws SQLException {
        final Connection connection = getConnection();
        connection.setAutoCommit(false);
        return new JdbcTransactedSession(connection);
    }

    @Override
    protected boolean isResourceSetAvailable(final ReadableResourceSet.Id<?, ?> id) {
        if (StatementTemplate.class != id.getKeyType()) {
            log.warn("JDBC ResourceSets have key type StatementTemplate, not {}", id.getKeyType().getName());
            return false;
        }
        if (ResultSetRow.class != id.getElementType()) {
            log.warn("JDBC readable ResourceSets have element type ResultSetRow, not {}", id.getElementType().getName());
            return false;
        }
        return true;
    }

    @Override
    protected ReadableResourceSet<?, ?> resolveResourceSet(final ReadableResourceSet.Id<?, ?> id) {
        @SuppressWarnings("unchecked")
        final ReadableResourceSet.Id<StatementTemplate, ResultSetRow> resultId = (ReadableResourceSet.Id<StatementTemplate, ResultSetRow>) id;
        return new ReadableTable(resultId);
    }

    @Override
    protected boolean isResourceSetAvailable(final WritableResourceSet.Id<?, ?, ?> id) {
        if (StatementTemplate.class != id.getKeyType()) {
            log.warn("JDBC ResourceSets have key type StatementTemplate, not {}", id.getKeyType().getName());
            return false;
        }
        if (StatementParameters.class != id.getElementType()) {
            log.warn("JDBC writable ResourceSets have element type StatementParameters, not {}", id.getElementType().getName());
            return false;
        }
        if (Integer.class != id.getWriteResultType()) {
            log.warn("JDBC ResourceSets have write-result type Integer, not {}", id.getWriteResultType().getName());
            return false;
        }
        return true;
    }

    @Override
    protected WritableResourceSet<?, ?, ?> resolveResourceSet(final WritableResourceSet.Id<?, ?, ?> id) {
        @SuppressWarnings("unchecked")
        final WritableResourceSet.Id<StatementTemplate, StatementParameters, Integer> resultId = (WritableResourceSet.Id<StatementTemplate, StatementParameters, Integer>) id;
        return new WritableTable(resultId);
    }

    private Connection getConnection() throws SQLException {
        return this.username.isPresent()
            ? this.dataSource.getConnection(this.username.get(), this.password.get())
            : this.dataSource.getConnection();
    }

}
