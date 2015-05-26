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

import crud.core.MiddlewareException;
import crud.core.Session;
import rx.Observable;


/*package*/ abstract class SessionWrapper implements Session {

    private @Nonnull final javax.jms.Session delegate;


    @Override
    public Session.Ordering getOrdering() {
        try {
            final boolean isTransacted = this.delegate.getTransacted();
            return isTransacted
                    ? Session.Ordering.TRANSACTED
                    : Session.Ordering.ORDERED;
        } catch (final JMSException jx) {
            throw new MiddlewareException(jx.getMessage(), jx);
        }
    }

    @Override
    public Observable<Void> shutdown() {
        try {
            this.delegate.close();
            return Observable.empty();
        } catch (final JMSException jx) {
            return Observable.error(new MiddlewareException(jx.getMessage(), jx));
        }
    }

    protected SessionWrapper(@Nonnull final javax.jms.Session delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    protected final @Nonnull javax.jms.Session getDelegate() {
        return this.delegate;
    }

}
