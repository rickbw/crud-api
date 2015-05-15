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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import crud.core.DataBus;
import crud.core.DataSet;
import crud.core.DataSetId;
import crud.core.MiddlewareException;
import rx.Observable;


public class JdbcDataBus implements DataBus {

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
        try {
            // TODO: Move Connection to "session" concept:
            final Connection connection = this.username.isPresent()
                ? this.dataSource.getConnection(this.username.get(), this.password.get())
                : this.dataSource.getConnection();

            // TODO: What state should be encapsulated in the Table?
            final DataSet<K, E> table = new Table<>(id);
            return Optional.of(table);
        } catch (final SQLException sqx) {
            throw new MiddlewareException(sqx.getMessage(), sqx);
        }
    }

    @Override
    public Observable<Void> shutdown() {
        return Observable.empty();  // nothing to do
    }

}
