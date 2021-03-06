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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import crud.core.ReadableResource;
import crud.core.ReadableResourceTest;
import crud.implementer.SessionWorker;
import rx.Observable;


public class TextLineFileReaderTest extends ReadableResourceTest<String> {

    private static final ImmutableList<String> lines = ImmutableList.of(
            "foo=bar bax=quux",
            "hello, world");

    private final SessionWorker worker = SessionWorker.create();
    private File file;


    @Before
    public void setup() throws IOException {
        this.file = File.createTempFile(getClass().getSimpleName(), null);
        try (Writer writer = new FileWriter(this.file)) {
            for (final String line : lines) {
                writer.write(line + '\n');
            }
        }
    }

    @Test
    public void fileContentsExpected() {
        // given:
        final ReadableResource<String> resource = createDefaultResource();

        // when:
        final Observable<String> result = resource.read();

        // then:
        final Iterator<String> actualLines = result.toBlocking().getIterator();
        for (final String expectedLine : lines) {
            assertEquals(expectedLine, actualLines.next());
        }
        assertFalse(actualLines.hasNext());
    }

    @Override
    protected ReadableResource<String> createDefaultResource() {
        return new TextLineFileReader(this.file, this.worker);
    }

}
