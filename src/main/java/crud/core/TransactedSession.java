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
package crud.core;

import javax.annotation.Nonnull;

import rx.Observable;
import rx.Observer;


/**
 * A {@link Session} in which operations are not only ordered, but occur in
 * atomic groups, such that failures may be {@link #rollback() rolled back}
 * and re-observed.
 *
 * @author Rick Warren
 */
public interface TransactedSession extends Session {

    /**
     * @return {@link crud.core.Session.Ordering#TRANSACTED}
     */
    @Override
    public @Nonnull Session.Ordering getOrdering();

    /**
     * Commit all data observations, both reads and writes, that previously
     * occurred in the context of this {@link TransactedSession}. They will
     * not be repeated.
     * <p/>
     * The commit commences immediately with the call to this method; it
     * does not require the resulting {@link Observable} to be subscribed.
     * That Observable behaves as if {@link Observable#cache() cached}: the
     * same result will be emitted to any subscriber.
     *
     * @return  An {@link Observable} that will
     *          {@link Observer#onCompleted() complete} when the commit has
     *          taken place, or otherwise reflect any failure that occurred.
     */
    public Observable<Void> commit();

    /**
     * Roll back all data observations, both reads and writes, that previously
     * occurred in the context of this {@link TransactedSession}, such that
     * they may be repeated.
     * <p/>
     * The roll-back commences immediately with the call to this method; it
     * does not require the resulting {@link Observable} to be subscribed.
     * That Observable behaves as if {@link Observable#cache() cached}: the
     * same result will be emitted to any subscriber.
     *
     * @return  An {@link Observable} that will
     *          {@link Observer#onCompleted() complete} when the roll-back has
     *          taken place, or otherwise reflect any failure that occurred.
     */
    public Observable<Void> rollback();

}
