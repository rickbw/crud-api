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
package crud.implementer;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import crud.core.Session;


/**
 * A very simple reusable {@link Session} implementation, supporting
 * {@link crud.core.Session.Ordering#UNORDERED unordered} and
 * {@link crud.core.Session.Ordering#ORDERED ordered} sessions.
 *
 * @author Rick Warren
 */
public class DefaultSession extends AbstractSession {

    public DefaultSession(
            @Nonnull final DataBusWorker dataBusWorker,
            @Nonnull final Session.Ordering ordering) {
        super(dataBusWorker, SessionWorker.create(), ordering);
        Preconditions.checkArgument(
                getOrdering() != Session.Ordering.TRANSACTED,
                "Not a TransactedSession");
    }

}
