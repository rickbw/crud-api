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
package crud.sync;

import com.google.common.base.Throwables;

import crud.core.TransactedSession;
import crud.implementer.AsyncResults;
import rx.Observable;


/**
 * @see TransactedSession
 *
 * @author Rick Warren
 */
public class SyncTransactedSession extends SyncSession {

    public SyncTransactedSession(final TransactedSession delegate) {
        super(delegate);
    }

    /**
     * @see TransactedSession#commit()
     */
    public void commit() {
        final Observable<Void> committed = ((TransactedSession) getDelegate()).commit();
        try {
            AsyncResults.awaitCompletion(committed);
        } catch (final Exception ex) {
            Throwables.propagate(ex);
        }
    }

    /**
     * @see TransactedSession#rollback()
     */
    public void rollback() {
        final Observable<Void> committed = ((TransactedSession) getDelegate()).rollback();
        try {
            AsyncResults.awaitCompletion(committed);
        } catch (final Exception ex) {
            Throwables.propagate(ex);
        }
    }
}
