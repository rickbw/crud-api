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
import javax.annotation.Nullable;
import javax.jms.Destination;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import com.google.common.base.Strings;

import crud.core.MiddlewareException;
import rx.functions.Func1;


public class JndiDestinationLookupFunction implements Func1<String, Destination> {

    private @Nonnull final Context context;


    public JndiDestinationLookupFunction(@Nonnull final Context context) {
        this.context = Objects.requireNonNull(context);
    }

    @Override
    public @Nullable Destination call(@Nullable final String name) {
        if (Strings.isNullOrEmpty(name)) {
            /* Context forbids null names, and treats the empty name as an
             * identifier for the Context itself.
             */
            return null;
        }
        try {
            return (Destination) this.context.lookup(name);
        } catch (final NameNotFoundException nnfx) {
            return null;
        } catch (final NamingException nx) {
            throw new MiddlewareException(nx.getMessage(), nx);
        }
    }

}
