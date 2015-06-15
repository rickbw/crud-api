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
package crud.file;

import java.io.File;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import crud.core.ReadableResourceSet;
import crud.core.Session;
import crud.core.Session.Ordering;
import crud.core.WritableResourceSet;
import crud.implementer.AbstractDataBus;


/**
 * Accesses the file system.
 *
 * @author Rick Warren
 */
public final class FileSystem extends AbstractDataBus {

    private static final Set<Ordering> supportedOrderings = Collections.unmodifiableSet(EnumSet.of(
            Session.Ordering.UNORDERED,
            Session.Ordering.ORDERED));


    @Override
    public Set<Session.Ordering> getSupportedSessionOrderings() {
        return supportedOrderings;
    }

    @Override
    protected Session doStartOrderedSession() throws Exception {
        return new FileSession();
    }

    @Override
    protected boolean isResourceSetAvailable(final ReadableResourceSet.Id<?, ?> id) {
        return File.class == id.getKeyType()
            && String.class == id.getResourceType();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" }) // checked by isResourceSetAvailable()
    @Override
    protected ReadableResourceSet resolveResourceSet(final ReadableResourceSet.Id id) {
        return new ReadableFileSet(id);
    }

    @Override
    protected boolean isResourceSetAvailable(final WritableResourceSet.Id<?, ?, ?> id) {
        return WriteRequest.class == id.getKeyType()
            && String.class == id.getResourceType()
            && Void.class == id.getWriteResultType();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" }) // checked by isResourceSetAvailable()
    @Override
    protected WritableResourceSet resolveResourceSet(final WritableResourceSet.Id id) {
        return new WritableFileSet(id);
    }

}
