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

import java.io.IOException;


/**
 * Indicates that a problem occurred communicating with the target middleware.
 * For convenience, this exception is unchecked. Check analogs include
 * {@link IOException}, {@link java.sql.SQLException}, and
 * {@javax.jms.JMSException}.
 *
 * @author Rick Warren
 */
public class MiddlewareException extends RuntimeException {

    private static final long serialVersionUID = 3352973915205266653L;


    public MiddlewareException() {
        super();
    }

    public MiddlewareException(final String message) {
        super(message);
    }

    public MiddlewareException(final Throwable cause) {
        super(cause);
    }

    public MiddlewareException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MiddlewareException(
            final String message,
            final Throwable cause,
            final boolean enableSuppression,
            final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
