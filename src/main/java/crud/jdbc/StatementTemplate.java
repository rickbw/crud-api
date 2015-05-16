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

import com.google.common.collect.ImmutableList;


/**
 * An immutable representation of a {@link PreparedStatement} with its
 * {@link Parameter parameters}. Also serves as a factory for
 * {@code PreparedStatement}s; see {@link #prepareStatement(Connection)}.
 *
 * @author Rick Warren
 */
public final class StatementTemplate {

    private final String sql;
    private final ImmutableList<Parameter> parameters;


    /**
     * Create a statement with no parameters.
     */
    public static StatementTemplate fromSimpleSql(@Nonnull final String sql) {
        return new StatementTemplate(sql, ImmutableList.<Parameter>of());
    }

    /**
     * Begin the construction of a statement with parameters. Continue the
     * construction with {@link Builder#substitute(Parameter)}.
     */
    public static Builder fromSql(@Nonnull final String sql) {
        return new Builder(sql);
    }

    public @Nonnull PreparedStatement prepareStatement(final Connection connection)
    throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(this.sql);
        for (final Parameter param : this.parameters) {
            param.apply(statement);
        }
        return statement;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final StatementTemplate other = (StatementTemplate) obj;
        if (!this.sql.equals(other.sql)) {
            return false;
        }
        if (!this.parameters.equals(other.parameters)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.sql.hashCode();
        result = prime * result + this.parameters.hashCode();
        return result;
    }

    private StatementTemplate(@Nonnull final String sql, final Iterable<Parameter> params) {
        this.sql = Objects.requireNonNull(sql);
        this.parameters = ImmutableList.copyOf(params);
    }


    public static final class Builder {
        private final String sql;
        private final ImmutableList.Builder<Parameter> parameters = ImmutableList.builder();

        public Builder substitute(@Nonnull final Parameter param) {
            this.parameters.add(param);
            return this;
        }

        public StatementTemplate build() {
            return new StatementTemplate(this.sql, this.parameters.build());
        }

        private Builder(@Nonnull final String sql) {
            this.sql = Objects.requireNonNull(sql);
        }
    }

}
