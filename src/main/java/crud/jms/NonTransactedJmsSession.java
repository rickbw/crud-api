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


/*package*/ final class NonTransactedJmsSession extends SessionWrapper {

    private static final Logger log = LoggerFactory.getLogger(NonTransactedJmsSession.class);


    public NonTransactedJmsSession(final Session delegate) {
        super(delegate);
        try {
            Preconditions.checkArgument(
                    !getDelegate().getTransacted(),
                    "Session must not be transacted");
        } catch (final JMSException jx) {
            log.warn("Unable to determine whether Session is transacted; assuming NO", jx);
        }
    }

}
