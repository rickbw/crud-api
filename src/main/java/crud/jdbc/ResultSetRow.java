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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;


/**
 * A JDBC {@link ResultSet} that that wraps another, and only exposes its data
 * accessors. It does not allow the "cursor" to be advanced to other rows,
 * but it does not prevent that cursor to be advanced underneath. Clients that
 * wish to keep a reference to state exposed by a row should get it out before
 * relinquishing control.
 *
 * @author Rick Warren
 */
public class ResultSetRow implements ResultSet {

    private @Nonnull final ResultSet delegate;


    @Override
    public boolean wasNull() throws SQLException {
        return this.delegate.wasNull();
    }

    @Override
    public String getString(final int columnIndex) throws SQLException {
        return this.delegate.getString(columnIndex);
    }

    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return this.delegate.getBoolean(columnIndex);
    }

    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return this.delegate.getByte(columnIndex);
    }

    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return this.delegate.getShort(columnIndex);
    }

    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return this.delegate.getInt(columnIndex);
    }

    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return this.delegate.getLong(columnIndex);
    }

    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return this.delegate.getFloat(columnIndex);
    }

    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return this.delegate.getDouble(columnIndex);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return this.delegate.getBigDecimal(columnIndex, scale);
    }

    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return this.delegate.getBytes(columnIndex);
    }

    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return this.delegate.getDate(columnIndex);
    }

    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return this.delegate.getTime(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return this.delegate.getTimestamp(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return this.delegate.getAsciiStream(columnIndex);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return this.delegate.getUnicodeStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return this.delegate.getBinaryStream(columnIndex);
    }

    @Override
    public String getString(final String columnLabel) throws SQLException {
        return this.delegate.getString(columnLabel);
    }

    @Override
    public boolean getBoolean(final String columnLabel) throws SQLException {
        return this.delegate.getBoolean(columnLabel);
    }

    @Override
    public byte getByte(final String columnLabel) throws SQLException {
        return this.delegate.getByte(columnLabel);
    }

    @Override
    public short getShort(final String columnLabel) throws SQLException {
        return this.delegate.getShort(columnLabel);
    }

    @Override
    public int getInt(final String columnLabel) throws SQLException {
        return this.delegate.getInt(columnLabel);
    }

    @Override
    public long getLong(final String columnLabel) throws SQLException {
        return this.delegate.getLong(columnLabel);
    }

    @Override
    public float getFloat(final String columnLabel) throws SQLException {
        return this.delegate.getFloat(columnLabel);
    }

    @Override
    public double getDouble(final String columnLabel) throws SQLException {
        return this.delegate.getDouble(columnLabel);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(final String columnLabel, final int scale) throws SQLException {
        return this.delegate.getBigDecimal(columnLabel, scale);
    }

    @Override
    public byte[] getBytes(final String columnLabel) throws SQLException {
        return this.delegate.getBytes(columnLabel);
    }

    @Override
    public Date getDate(final String columnLabel) throws SQLException {
        return this.delegate.getDate(columnLabel);
    }

    @Override
    public Time getTime(final String columnLabel) throws SQLException {
        return this.delegate.getTime(columnLabel);
    }

    @Override
    public Timestamp getTimestamp(final String columnLabel) throws SQLException {
        return this.delegate.getTimestamp(columnLabel);
    }

    @Override
    public InputStream getAsciiStream(final String columnLabel) throws SQLException {
        return this.delegate.getAsciiStream(columnLabel);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(final String columnLabel) throws SQLException {
        return this.delegate.getUnicodeStream(columnLabel);
    }

    @Override
    public InputStream getBinaryStream(final String columnLabel) throws SQLException {
        return this.delegate.getBinaryStream(columnLabel);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.delegate.getWarnings();
    }

    @Override
    public void clearWarnings() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public String getCursorName() throws SQLException {
        return this.delegate.getCursorName();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this.delegate.getMetaData();
    }

    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return this.delegate.getObject(columnIndex);
    }

    @Override
    public Object getObject(final String columnLabel) throws SQLException {
        return this.delegate.getObject(columnLabel);
    }

    @Override
    public int findColumn(final String columnLabel) throws SQLException {
        return this.delegate.findColumn(columnLabel);
    }

    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return this.delegate.getCharacterStream(columnIndex);
    }

    @Override
    public Reader getCharacterStream(final String columnLabel) throws SQLException {
        return this.delegate.getCharacterStream(columnLabel);
    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return this.delegate.getBigDecimal(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(final String columnLabel) throws SQLException {
        return this.delegate.getBigDecimal(columnLabel);
    }

    @Override
    public int getRow() throws SQLException {
        return this.delegate.getRow();
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return this.delegate.getFetchDirection();
    }

    @Override
    public int getFetchSize() throws SQLException {
        return this.delegate.getFetchSize();
    }

    @Override
    public int getType() throws SQLException {
        return this.delegate.getType();
    }

    @Override
    public int getConcurrency() throws SQLException {
        return this.delegate.getConcurrency();
    }

    @Override
    public void refreshRow() throws SQLException {
        this.delegate.refreshRow();
    }

    @Override
    public Object getObject(final int columnIndex, final Map<String, Class<?>> map) throws SQLException {
        return this.delegate.getObject(columnIndex, map);
    }

    @Override
    public Ref getRef(final int columnIndex) throws SQLException {
        return this.delegate.getRef(columnIndex);
    }

    @Override
    public Blob getBlob(final int columnIndex) throws SQLException {
        return this.delegate.getBlob(columnIndex);
    }

    @Override
    public Clob getClob(final int columnIndex) throws SQLException {
        return this.delegate.getClob(columnIndex);
    }

    @Override
    public Array getArray(final int columnIndex) throws SQLException {
        return this.delegate.getArray(columnIndex);
    }

    @Override
    public Object getObject(final String columnLabel, final Map<String, Class<?>> map) throws SQLException {
        return this.delegate.getObject(columnLabel, map);
    }

    @Override
    public Ref getRef(final String columnLabel) throws SQLException {
        return this.delegate.getRef(columnLabel);
    }

    @Override
    public Blob getBlob(final String columnLabel) throws SQLException {
        return this.delegate.getBlob(columnLabel);
    }

    @Override
    public Clob getClob(final String columnLabel) throws SQLException {
        return this.delegate.getClob(columnLabel);
    }

    @Override
    public Array getArray(final String columnLabel) throws SQLException {
        return this.delegate.getArray(columnLabel);
    }

    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return this.delegate.getDate(columnIndex, cal);
    }

    @Override
    public Date getDate(final String columnLabel, final Calendar cal) throws SQLException {
        return this.delegate.getDate(columnLabel, cal);
    }

    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return this.delegate.getTime(columnIndex, cal);
    }

    @Override
    public Time getTime(final String columnLabel, final Calendar cal) throws SQLException {
        return this.delegate.getTime(columnLabel, cal);
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return this.delegate.getTimestamp(columnIndex, cal);
    }

    @Override
    public Timestamp getTimestamp(final String columnLabel, final Calendar cal) throws SQLException {
        return this.delegate.getTimestamp(columnLabel, cal);
    }

    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return this.delegate.getURL(columnIndex);
    }

    @Override
    public URL getURL(final String columnLabel) throws SQLException {
        return this.delegate.getURL(columnLabel);
    }

    @Override
    public RowId getRowId(final int columnIndex) throws SQLException {
        return this.delegate.getRowId(columnIndex);
    }

    @Override
    public RowId getRowId(final String columnLabel) throws SQLException {
        return this.delegate.getRowId(columnLabel);
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.delegate.getHoldability();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.delegate.isClosed();
    }

    @Override
    public NClob getNClob(final int columnIndex) throws SQLException {
        return this.delegate.getNClob(columnIndex);
    }

    @Override
    public NClob getNClob(final String columnLabel) throws SQLException {
        return this.delegate.getNClob(columnLabel);
    }

    @Override
    public SQLXML getSQLXML(final int columnIndex) throws SQLException {
        return this.delegate.getSQLXML(columnIndex);
    }

    @Override
    public SQLXML getSQLXML(final String columnLabel) throws SQLException {
        return this.delegate.getSQLXML(columnLabel);
    }

    @Override
    public void updateNString(final int columnIndex, final String nString) throws SQLException {
        this.delegate.updateNString(columnIndex, nString);
    }

    @Override
    public String getNString(final int columnIndex) throws SQLException {
        return this.delegate.getNString(columnIndex);
    }

    @Override
    public String getNString(final String columnLabel) throws SQLException {
        return this.delegate.getNString(columnLabel);
    }

    @Override
    public Reader getNCharacterStream(final int columnIndex) throws SQLException {
        return this.delegate.getNCharacterStream(columnIndex);
    }

    @Override
    public Reader getNCharacterStream(final String columnLabel) throws SQLException {
        return this.delegate.getNCharacterStream(columnLabel);
    }

    @Override
    public <T> T getObject(final int columnIndex, final Class<T> type) throws SQLException {
        return this.delegate.getObject(columnIndex, type);
    }

    @Override
    public <T> T getObject(final String columnLabel, final Class<T> type) throws SQLException {
        return this.delegate.getObject(columnLabel, type);
    }


    // --- Hard-coded methods: -----------------------------------------------

    /**
     * @return false, always
     *
     * @see #unwrap(Class)
     */
    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        return false;
    }

    /**
     * @throws SQLException always: this {@link ResultSet} does not wrap
     *                      any other type.
     *
     * @see #isWrapperFor(Class)
     */
    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @return false, always.
     */
    @Override
    public boolean rowUpdated() throws SQLException {
        return false;
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @return false, always.
     */
    @Override
    public boolean rowInserted() throws SQLException {
        return false;
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @return false, always.
     */
    @Override
    public boolean rowDeleted() throws SQLException {
        return false;
    }

    /**
     * Does nothing.
     */
    @Override
    public void cancelRowUpdates() throws SQLException {
        // do nothing
    }

    /**
     * @return null, always.
     */
    @Override
    public Statement getStatement() throws SQLException {
        return null;
    }

    /**
     * Does nothing. Clients of this {@link ResultSet} do not own the data
     * they view, and so are not allowed to close it.
     */
    @Deprecated
    @Override
    public void close() throws SQLException {
        // do nothing
    }


    // --- Unsupported methods: ----------------------------------------------

    /**
     * This {@link ResultSet} subtype does not allow the cursor to be moved.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean next() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean isBeforeFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean isAfterLast() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean isFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean isLast() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void beforeFirst() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void afterLast() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean first() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean last() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean absolute(final int row) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean relative(final int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public boolean previous() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void setFetchSize(final int rows) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNull(final int columnIndex) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateByte(final int columnIndex, final byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateShort(final int columnIndex, final short x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateInt(final int columnIndex, final int x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateLong(final int columnIndex, final long x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateFloat(final int columnIndex, final float x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateDouble(final int columnIndex, final double x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateString(final int columnIndex, final String x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateDate(final int columnIndex, final Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateTime(final int columnIndex, final Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateObject(final int columnIndex, final Object x, final int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateObject(final int columnIndex, final Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNull(final String columnLabel) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBoolean(final String columnLabel, final boolean x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateByte(final String columnLabel, final byte x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateShort(final String columnLabel, final short x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateInt(final String columnLabel, final int x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateLong(final String columnLabel, final long x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateFloat(final String columnLabel, final float x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateDouble(final String columnLabel, final double x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBigDecimal(final String columnLabel, final BigDecimal x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateString(final String columnLabel, final String x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBytes(final String columnLabel, final byte[] x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateDate(final String columnLabel, final Date x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateTime(final String columnLabel, final Time x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateTimestamp(final String columnLabel, final Timestamp x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final int length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateObject(final String columnLabel, final Object x, final int scaleOrLength) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateObject(final String columnLabel, final Object x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void insertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateRef(final int columnIndex, final Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateRef(final String columnLabel, final Ref x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBlob(final int columnIndex, final Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBlob(final String columnLabel, final Blob x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateClob(final int columnIndex, final Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateClob(final String columnLabel, final Clob x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateArray(final int columnIndex, final Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateArray(final String columnLabel, final Array x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateRowId(final int columnIndex, final RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateRowId(final String columnLabel, final RowId x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNString(final String columnLabel, final String nString) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNClob(final int columnIndex, final NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNClob(final String columnLabel, final NClob nClob) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateSQLXML(final int columnIndex, final SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateSQLXML(final String columnLabel, final SQLXML xmlObject) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNClob(final int columnIndex, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNClob(final String columnLabel, final Reader reader, final long length) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateAsciiStream(final String columnLabel, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBinaryStream(final String columnLabel, final InputStream x) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateCharacterStream(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBlob(final int columnIndex, final InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateBlob(final String columnLabel, final InputStream inputStream) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateClob(final int columnIndex, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateClob(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNClob(final int columnIndex, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void updateNClob(final String columnLabel, final Reader reader) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * This {@link ResultSet} subtype does not support modification.
     *
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void deleteRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    /**
     * @throws SQLFeatureNotSupportedException  Always.
     */
    @Deprecated
    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }


    // --- Non-public implementation: ----------------------------------------

    /*package*/ ResultSetRow(@Nonnull final ResultSet delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

}
