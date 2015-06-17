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
package crud.jms;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.jms.JMSException;

import crud.core.Session;
import crud.implementer.AbstractSession;
import crud.implementer.SessionWorker;


/*package*/ abstract class SessionWrapper extends AbstractSession {

    private @Nonnull final javax.jms.Session delegate;


    protected SessionWrapper(@Nonnull final Session.Ordering ordering, @Nonnull final javax.jms.Session delegate) {
        super(SessionWorker.create(), ordering);
        this.delegate = Objects.requireNonNull(delegate);
    }

    protected final @Nonnull javax.jms.Session getDelegate() {
        return this.delegate;
    }

    @Override
    protected void doShutdown() throws JMSException {
        getDelegate().close();
    }

}
