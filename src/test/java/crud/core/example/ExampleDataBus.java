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
package crud.core.example;

import java.io.Writer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import crud.core.ReadableResourceSet;
import crud.core.Session;
import crud.core.WritableResourceSet;
import crud.implementer.AbstractDataBus;


public class ExampleDataBus extends AbstractDataBus {

    @Override
    public Set<Session.Ordering> getSupportedSessionOrderings() {
        return Collections.unmodifiableSet(EnumSet.of(
                Session.Ordering.UNORDERED,
                Session.Ordering.ORDERED));
    }

    @Override
    protected Session doStartOrderedSession() {
        return new ExampleSession();
    }

    @Override
    protected boolean isResourceSetAvailable(final ReadableResourceSet.Id<?, ?> id) {
        return id.getKeyType() == id.getResourceType();
    }

    @Override
    protected ReadableResourceSet<?, ?> resolveResourceSet(final ReadableResourceSet.Id<?, ?> id) {
        // isResourceSetAvailable() ensures type conversion is safe:
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final EchoReadableResourceSet result = new EchoReadableResourceSet(id);
        return result;
    }

    @Override
    protected boolean isResourceSetAvailable(final WritableResourceSet.Id<?, ?, ?> id) {
        return Writer.class.isAssignableFrom(id.getKeyType())
            && String.class.isAssignableFrom(id.getResourceType())
            && id.getWriteResultType().isAssignableFrom(Integer.class);
    }

    @Override
    protected WritableResourceSet<?, ?, ?> resolveResourceSet(final WritableResourceSet.Id<?, ?, ?> id) {
        @SuppressWarnings("rawtypes")
        final WritableResourceSet.Id rawId = id;
        @SuppressWarnings("unchecked")
        final WritableResourceSet.Id<Writer, String, Integer> typedId = rawId;
        return new PrintingResourceSet(typedId);
    }

}
