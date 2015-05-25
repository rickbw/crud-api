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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.NotThreadSafe;

import com.google.common.collect.ImmutableList;


@Immutable
public final class StatementParameters {

    private final ImmutableList<StatementParameter> params;


    public static StatementParameters of(final Iterable<StatementParameter> params) {
        return new StatementParameters(params);
    }

    public static StatementParameters of(final StatementParameter... params) {
        return of(Arrays.asList(params));
    }

    public static StatementParameters none() {
        return new StatementParameters(ImmutableList.<StatementParameter>of());
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String toString() {
        final StringBuilder buf = new StringBuilder(getClass().getSimpleName());
        buf.append('[');

        for (int i = 0; i < this.params.size(); ++i) {
            buf.append(this.params.get(i));
            if (i < this.params.size() - 1) {   // if not last
                buf.append(", ");
            }
        }

        buf.append(']');
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
        final StatementParameters other = (StatementParameters) obj;
        return this.params.equals(other.params);
    }

    @Override
    public int hashCode() {
        return 31 + this.params.hashCode();
    }

    /*package*/ void substituteAll(final PreparedStatement statement) throws SQLException {
        for (final StatementParameter param : this.params) {
            param.substitute(statement);
        }
    }

    /*package*/ String substituteString(final String statement) {
        if (!this.params.isEmpty()) {
            final StringBuilder buf = new StringBuilder(statement);
            for (final StatementParameter param : this.params) {
                final String substitution = param.toShortString();
                final int paramIndex = buf.indexOf("?");
                buf.replace(paramIndex, paramIndex + 1, substitution);
            }
            return buf.toString();
        } else {
            return statement;
        }
    }

    private StatementParameters(final Iterable<StatementParameter> params) {
        this.params = ImmutableList.copyOf(params);
    }


    @NotThreadSafe
    public static final class Builder {
        private final List<StatementParameter> params = new ArrayList<>();
        /**
         * JDBC parameter indexes start with 1.
         */
        private int nextIndex = 1;

        public Builder append(final StatementParameter.Builder param) {
            final StatementParameter newParam = param.atIndex(this.nextIndex++);
            this.params.add(newParam);
            checkInvariants();
            return this;
        }

        public Builder append(final StatementParameter param) {
            if (param.getIndex() == this.nextIndex) {
                this.params.add(param);
                ++this.nextIndex;
            } else if (param.getIndex() < this.nextIndex) {
                this.params.set(param.getIndex() - 1 /*1-based to 0-based*/, param);
            } else {
                throw new IndexOutOfBoundsException(
                        "Unspecified parameters between " + this.nextIndex + " and " + param.getIndex());
            }
            checkInvariants();
            return this;
        }

        public StatementParameters build() {
            return new StatementParameters(this.params);
        }

        private Builder() {
            checkInvariants();
        }

        private void checkInvariants() {
            assert this.params.size() == this.nextIndex - 1;
        }
    }

}
