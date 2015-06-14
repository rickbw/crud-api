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
import java.io.IOException;

import crud.core.ResourceSetTest;
import crud.core.WritableResourceSet;


public class WritableFileSetTest extends ResourceSetTest<File> {

    @Override
    protected WritableFileSet createDefaultResourceSet() {
        return new WritableFileSet(new WritableResourceSet.Id<>("Test", File.class, String.class, Void.class));
    }

    @Override
    protected File createDefaultKey() {
        try {
            return File.createTempFile(getClass().getSimpleName(), null);
        } catch (final IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
