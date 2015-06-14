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

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;


/**
 * An indexed parameter in a JDBC SQL statement string ("?").
 *
 * @see PreparedStatement#setObject(int, Object, int)
 *
 * @author Rick Warren
 */
@Immutable
public final class StatementParameter {

    private @Nullable final Object value;
    private final Optional<Type> type;
    private final int index;


    public static Builder ofValue(@Nullable final Object value) {
        return new Builder(value);
    }

    public static Builder ofNull() {
        return new Builder(null);
    }

    /**
     * Convenience method that preassigns the {@link Type} to
     * {@link Type#VARCHAR}.
     */
    public static Builder ofString(@Nullable final String value) {
        return new Builder(value).ofType(Type.VARCHAR);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofCharacter(@Nullable final Character value) {
        return new Builder(value).ofType(Type.CHARACTER);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofBoolean(@Nullable final Boolean value) {
        return new Builder(value).ofType(Type.BOOLEAN);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofByte(@Nullable final Byte value) {
        return new Builder(value).ofType(Type.BYTE);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofShort(@Nullable final Short value) {
        return new Builder(value).ofType(Type.SHORT);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofInteger(@Nullable final Integer value) {
        return new Builder(value).ofType(Type.INTEGER);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofLong(@Nullable final Long value) {
        return new Builder(value).ofType(Type.LONG);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofFloat(@Nullable final Float value) {
        return new Builder(value).ofType(Type.FLOAT);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofDouble(@Nullable final Double value) {
        return new Builder(value).ofType(Type.DOUBLE);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofBinary(@Nullable final byte[] value) {
        return new Builder(value).ofType(Type.BINARY);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofJavaObject(@Nullable final Serializable value) {
        return new Builder(value).ofType(Type.JAVA_OBJECT);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofDate(@Nullable final Date value) {
        return new Builder(value).ofType(Type.DATE);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofTime(@Nullable final Date value) {
        return new Builder(value).ofType(Type.TIME);
    }

    /**
     * Convenience method that preassigns the {@link Type}.
     */
    public static Builder ofTimestamp(@Nullable final Date value) {
        return new Builder(value).ofType(Type.TIMESTAMP);
    }

    @Override
    public final String toString() {
        final StringBuilder buf = new StringBuilder(getClass().getSimpleName());
        toShortString(buf);
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
        final StatementParameter other = (StatementParameter) obj;
        // Compare fields in order of cheapness:
        if (this.index != other.index) {
            return false;
        }
        if (!this.type.equals(other.type)) {
            return false;
        }
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Objects.hashCode(this.value);
        result = prime * result + this.type.hashCode();
        result = prime * result + this.index;
        return result;
    }

    /**
     * Set this parameter on the given {@link PreparedStatement}.
     */
    /*package*/ void substitute(final PreparedStatement statement) throws SQLException {
        if (this.type.isPresent()) {
            if (this.value != null) {
                statement.setObject(this.index, this.value, this.type.get().targetSqlType);
            } else {
                statement.setNull(this.index, this.type.get().targetSqlType);
            }
        } else {
            // DB may or may not support setting NULL that's not explicitly typed. Try.
            statement.setObject(this.index, this.value);
        }
    }

    /*package*/ int getIndex() {
        return this.index;
    }

    /*package*/ String toShortString() {
        final StringBuilder buf = new StringBuilder();
        toShortString(buf);
        return buf.toString();
    }

    private StatementParameter(@Nullable final Object value, final Optional<Type> type, final int index) {
        this.value = value;
        this.type = Objects.requireNonNull(type);
        this.index = index;
        Preconditions.checkArgument(this.index >= 1, "Indexes start at 1");
    }

    private void toShortString(final StringBuilder buf) {
        buf.append('{');
        buf.append(this.index).append(": ").append(this.value);
        if (this.type.isPresent()) {
            buf.append(" as ").append(this.type.get().name());
        }
        buf.append('}');
    }


    public static enum Type {
        /**
         * @see Types#BINARY
         */
        BINARY(Types.BINARY),
        /**
         * @see Types#BIT
         */
        BIT(Types.BIT),
        /**
         * @see Types#TINYINT
         */
        BYTE(Types.TINYINT),
        /**
         * @see Types#CHAR
         */
        CHARACTER(Types.CHAR),
        /**
         * @see Types#DATE
         */
        DATE(Types.DATE),
        /**
         * @see Types#DECIMAL
         */
        DECIMAL(Types.DECIMAL),
        /**
         * @see Types#DOUBLE
         */
        DOUBLE(Types.DOUBLE),
        /**
         * @see Types#FLOAT
         */
        FLOAT(Types.FLOAT),
        /**
         * @see Types#INTEGER
         */
        INTEGER(Types.INTEGER),
        /**
         * @see Types#BIGINT
         */
        LONG(Types.BIGINT),
        /**
         * @see Types#LONGVARBINARY
         */
        LONGVARBINARY(Types.LONGVARBINARY),
        /**
         * @see Types#LONGVARCHAR
         */
        LONGVARCHAR(Types.LONGVARCHAR),
        /**
         * @see Types#NUMERIC
         */
        NUMERIC(Types.NUMERIC),
        /**
         * @see Types#REAL
         */
        REAL(Types.REAL),
        /**
         * @see Types#SMALLINT
         */
        SHORT(Types.SMALLINT),
        /**
         * @see Types#TIME
         */
        TIME(Types.TIME),
        /**
         * @see Types#TIMESTAMP
         */
        TIMESTAMP(Types.TIMESTAMP),
        /**
         * @see Types#VARBINARY
         */
        VARBINARY(Types.VARBINARY),
        /**
         * @see Types#VARCHAR
         */
        VARCHAR(Types.VARCHAR),
        /**
         * @see Types#NULL
         */
        NULL(Types.NULL),
        /**
         * @see Types#OTHER
         */
        OTHER(Types.OTHER),
        /**
         * @see Types#JAVA_OBJECT
         */
        JAVA_OBJECT(Types.JAVA_OBJECT),
        /**
         * @see Types#DISTINCT
         */
        DISTINCT(Types.DISTINCT),
        /**
         * @see Types#STRUCT
         */
        STRUCT(Types.STRUCT),
        /**
         * @see Types#ARRAY
         */
        ARRAY(Types.ARRAY),
        /**
         * @see Types#BLOB
         */
        BLOB(Types.BLOB),
        /**
         * @see Types#CLOB
         */
        CLOB(Types.CLOB),
        /**
         * @see Types#REF
         */
        REF(Types.REF),
        /**
         * @see Types#DATALINK
         */
        DATALINK(Types.DATALINK),
        /**
         * @see Types#BOOLEAN
         */
        BOOLEAN(Types.BOOLEAN),
        /**
         * @see Types#ROWID
         */
        ROWID(Types.ROWID),
        /**
         * @see Types#NCHAR
         */
        NCHAR(Types.NCHAR),
        /**
         * @see Types#NVARCHAR
         */
        NVARCHAR(Types.NVARCHAR),
        /**
         * @see Types#LONGNVARCHAR
         */
        LONGNVARCHAR(Types.LONGNVARCHAR),
        /**
         * @see Types#NCLOB
         */
        NCLOB(Types.NCLOB),
        /**
         * @see Types#SQLXML
         */
        SQLXML(Types.SQLXML);

        private final int targetSqlType;

        private Type(final int targetSqlType) {
            this.targetSqlType = targetSqlType;
        }
    }


    public static final class Builder {
        private @Nullable final Object value;
        private Optional<Type> type = Optional.absent();

        private Builder(@Nullable final Object value) {
            this.value = value;
        }

        public Builder ofType(final Type theType) {
            this.type = Optional.of(theType);
            return this;
        }

        public StatementParameter atIndex(final int position) {
            return new StatementParameter(this.value, this.type, position);
        }
    }

}
