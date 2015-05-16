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
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import crud.core.DataBus;
import crud.core.DataSet;
import crud.core.DataSetId;
import crud.core.MiddlewareException;
import crud.core.Session;
import rx.Observable;


public class JdbcDataBus implements DataBus {

    private static final Logger log = LoggerFactory.getLogger(JdbcDataBus.class);

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
    public void start() {
        // nothing to do
    }

    @Override
    public <K, E> Optional<DataSet<K, E>> dataSet(final DataSetId<K, E> id) {
        if (StatementTemplate.class != id.getKeyType()) {
            log.warn("JDBC DataSets have key type StatementTemplate, not {}", id.getKeyType().getName());
            return Optional.absent();
        }
        if (ResultSetRow.class != id.getElementType()) {
            // JDBC only support DataSets of type ResultSetRow
            log.warn("JDBC DataSets have element type ResultSetRow, not {}", id.getElementType().getName());
            return Optional.absent();
        }
        return createDataSet(id);
    }

    @SuppressWarnings("resource")
    @Override
    public Session startSession(final Session.Ordering requestedOrdering) {
        try {
            final Connection connection = this.username.isPresent()
                ? this.dataSource.getConnection(this.username.get(), this.password.get())
                : this.dataSource.getConnection();
            return new JdbcSession(connection, requestedOrdering);
        } catch (final SQLException sqx) {
            throw new MiddlewareException(sqx.getMessage(), sqx);
        }
    }

    @Override
    public Observable<Void> shutdown() {
        return Observable.empty();  // nothing to do
    }

    private static <K, E> Optional<DataSet<K, E>> createDataSet(final DataSetId<K, E> id) {
        /* All of these unchecked conversions are necessary, because the
         * method signature requires dynamic typing, but in this case, the
         * types are actually static.
         */
        @SuppressWarnings("unchecked")
        final DataSetId<StatementTemplate, ResultSetRow> resultSetDataSetId = (DataSetId<StatementTemplate, ResultSetRow>) id;
        final DataSet<StatementTemplate, ResultSetRow> table = new Table(resultSetDataSetId);
        @SuppressWarnings("rawtypes")
        final Optional untypedDataSet = Optional.of(table);
        @SuppressWarnings("unchecked")
        final Optional<DataSet<K, E>> typedDataSet = untypedDataSet;
        return typedDataSet;
    }

}
