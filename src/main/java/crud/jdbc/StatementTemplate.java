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
import javax.annotation.concurrent.Immutable;


/**
 * An immutable representation of a {@link PreparedStatement} with its
 * {@link StatementParameters parameters}.
 *
 * @author Rick Warren
 */
@Immutable
public final class StatementTemplate {

    private final String sql;
    private final StatementParameters parameters;


    public StatementTemplate(@Nonnull final String sql, @Nonnull final StatementParameters params) {
        this.sql = Objects.requireNonNull(sql);
        this.parameters = Objects.requireNonNull(params);
    }

    public StatementTemplate(@Nonnull final String sql) {
        this(sql, StatementParameters.none());
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(getClass().getSimpleName());
        buf.append('(');
        final String withSubstitutions = this.parameters.substituteString(this.sql);
        buf.append(withSubstitutions);
        buf.append(')');
        return buf.toString();
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

    /*package*/ @Nonnull PreparedStatement prepareStatement(final Connection connection)
    throws SQLException {
        final PreparedStatement statement = connection.prepareStatement(this.sql);
        this.parameters.substituteAll(statement);
        return statement;
    }

}
