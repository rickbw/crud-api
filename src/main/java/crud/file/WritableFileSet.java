/* Copyright 2014–2015 Rick Warren
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

import crud.core.Session;
import crud.core.WritableResource;
import crud.core.WritableResourceSet;
import crud.implementer.AbstractSession;
import crud.implementer.AbstractWritableResourceSet;
import crud.implementer.SessionWorker;


/*package*/ final class WritableFileSet extends AbstractWritableResourceSet<File, String, Void> {

    public WritableFileSet(final WritableResourceSet.Id<File, String, Void> id) {
        super(id);
    }

    @Override
    public WritableResource<String, Void> get(final File key, final Session session) {
        final SessionWorker worker = ((AbstractSession) session).getWorker();
        return new TextLineFileAppender(key, worker);
    }

}
