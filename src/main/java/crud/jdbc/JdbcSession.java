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

import javax.annotation.Nonnull;

import crud.core.MiddlewareException;
import crud.core.Session;
import rx.Observable;


public class JdbcSession implements Session {

    private @Nonnull final Connection connection;


    public JdbcSession(
            @Nonnull final Connection connection,
            @Nonnull final Session.Ordering ordering) {
        this.connection = connection;

        try {
            final boolean isTransactional = (ordering == Session.Ordering.TRANSACTIONAL);
            this.connection.setAutoCommit(!isTransactional);
        } catch (final SQLException sx) {
            throw new MiddlewareException(sx.getMessage(), sx);
        }
    }

    @Override
    public Session.Ordering getOrdering() {
        try {
            final boolean isAutoCommit = this.connection.getAutoCommit();
            return isAutoCommit ? Session.Ordering.ORDERED : Session.Ordering.TRANSACTIONAL;
        } catch (final SQLException sx) {
            throw new MiddlewareException(sx.getMessage(), sx);
        }
    }

    @Override
    public Observable<Void> commit() {
        try {
            this.connection.commit();
            return Observable.empty();
        } catch (final SQLException sx) {
            return Observable.error(new MiddlewareException(sx.getMessage(), sx));
        }
    }

    @Override
    public Observable<Void> rollback() {
        try {
            this.connection.rollback();
            return Observable.empty();
        } catch (final SQLException sx) {
            return Observable.error(new MiddlewareException(sx.getMessage(), sx));
        }
    }

    @Override
    public Observable<Void> stop() {
        try {
            this.connection.close();
            return Observable.empty();
        } catch (final SQLException sx) {
            return Observable.error(new MiddlewareException(sx.getMessage(), sx));
        }
    }

}
