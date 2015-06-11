/* Copyright 2014 Rick Warren
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
package crud.pattern;

import static crud.core.RxAssertions.assertObservablesEqual;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.ReadableResource;
import crud.core.WritableResource;
import rx.Observable;


public class ResourceMergerWithWriterTest extends ResourceMergerTest {

    private final WritableResource<Object, Object> mockWriter = mock(WritableResource.class);
    private final Observable<Object> mockWriterResponse = Observable.<Object>just("Goodbye");


    @Override
    @Before
    public void setup() {
        super.setup();
        when(this.mockWriter.write(any())).thenReturn(this.mockWriterResponse);
    }

    @Test
    public void mergeCallsWriter() {
        // given:
        final ResourceMerger<Object> merger = createDefaultMerger(super.mockReader);

        // when:
        final Observable<Object> result = merger.merge();

        // then:
        assertObservablesEqual(this.mockWriterResponse, result);
        verify(this.mockWriter).write(super.mockReaderState.toBlocking().single());
    }

    @Override
    protected ResourceMerger<Object> createDefaultMerger(final ReadableResource<Object> reader) {
        return ResourceMerger.withWriter(reader, this.mockWriter);
    }

}
