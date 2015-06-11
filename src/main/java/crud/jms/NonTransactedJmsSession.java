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

import crud.core.Session;


/*package*/ final class NonTransactedJmsSession extends SessionWrapper implements Session {

    public NonTransactedJmsSession(final javax.jms.Session delegate) {
        super(Session.Ordering.ORDERED, delegate);
        // Assumed, but illegal to check in this thread:
        //assert !getDelegate().getTransacted();
    }

}
