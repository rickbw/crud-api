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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import crud.core.WritableResource;
import crud.core.WritableResourceTest;
import crud.implementer.SessionWorker;
import rx.Observable;


public class TextLineFileWriterTest extends WritableResourceTest<String, Void> {

    private final SessionWorker worker = SessionWorker.create();
    private File file;


    @Before
    public void setup() throws IOException {
        this.file = File.createTempFile(getClass().getSimpleName(), null);
    }

    @Test
    public void linesWritten() throws IOException {
        // given:
        final WritableResource<String, Void> resource = createDefaultResource();
        final String expectedLine = createDefaultResourceState();
        final int numLines = 3;

        // when:
        for (int i = 0; i < numLines; ++i) {
            final Observable<Void> response = resource.write(expectedLine);
            final Iterator<?> it = response.toBlocking().getIterator();
            assertFalse(it.hasNext());
        }

        // then:
        try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
            for (int i = 0; i < numLines; ++i) {
                final String actualLine = reader.readLine();
                assertEquals(expectedLine, actualLine);
            }
            assertNull(reader.readLine());
        }
    }

    @Override
    protected TextLineFileWriter createDefaultResource() {
        return new TextLineFileWriter(this.file, true, this.worker);
    }

    @Override
    protected String createDefaultResourceState() {
        return "hello, world";
    }

}
