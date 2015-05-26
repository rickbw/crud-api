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

import javax.jms.JMSException;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import crud.core.MiddlewareException;
import crud.core.TransactedSession;
import rx.Observable;


/*package*/ final class TransactedJmsSession extends SessionWrapper implements TransactedSession {

    private static final Logger log = LoggerFactory.getLogger(TransactedJmsSession.class);


    public TransactedJmsSession(final Session delegate) {
        super(delegate);
        try {
            Preconditions.checkArgument(
                    getDelegate().getTransacted(),
                    "Session must be transacted");
        } catch (final JMSException jx) {
            log.warn("Unable to determine whether Session is transacted; assuming YES", jx);
        }
    }

    @Override
    public Observable<Void> commit() {
        try {
            getDelegate().commit();
            return Observable.empty();
        } catch (final JMSException jx) {
            return Observable.error(new MiddlewareException(jx.getMessage(), jx));
        }
    }

    @Override
    public Observable<Void> rollback() {
        try {
            getDelegate().rollback();
            return Observable.empty();
        } catch (final JMSException jx) {
            return Observable.error(new MiddlewareException(jx.getMessage(), jx));
        }
    }

}
