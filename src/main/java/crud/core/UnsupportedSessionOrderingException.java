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


/**
 * Indicates that the application requested a
 * {@link crud.core.Session.Ordering}, via
 * {@link DataBus#startSession(crud.core.Session.Ordering)}, that the DataBus
 * implementation does not support. <em>Note</em> that a DataBus
 * implementation is always free to silently <em>upgrade</em> the requested
 * ordering -- for example, if the application requested
 * {@link crud.core.Session.Ordering#UNORDERED}, the DataBus may return a
 * {@link Session} with {@link Session.Ordering#ORDERED}. This exception will
 * only be thrown when the application requires too-high a contract, and the
 * DataBus is unable to meet it: for example, if the DataBus supports only
 * {@link Session.Ordering#ORDERED}, and the application requests
 * {@link Session.Ordering#TRANSACTIONAL}.
 *
 * @author Rick Warren
 */
public class UnsupportedSessionOrderingException extends MiddlewareException {

    private static final long serialVersionUID = 370744321002078649L;


    public UnsupportedSessionOrderingException() {
        super();
    }

    public UnsupportedSessionOrderingException(final String message) {
        super(message);
    }

    public UnsupportedSessionOrderingException(final Throwable cause) {
        super(cause);
    }

    public UnsupportedSessionOrderingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public UnsupportedSessionOrderingException(
            final String message,
            final Throwable cause,
            final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
