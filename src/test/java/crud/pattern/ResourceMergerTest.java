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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.ReadableResource;
import rx.Observable;
import rx.functions.Func0;


public abstract class ResourceMergerTest {

    protected final ReadableResource<Object> mockReader = mock(ReadableResource.class);
    protected final Observable<Object> mockReaderState = Observable.<Object>just("Hello, World");


    @Before
    public void setup() {
        when(this.mockReader.read()).thenReturn(this.mockReaderState);
    }

    @Test
    public void functionBehavesLikeMerge() {
        // given:
        final ResourceMerger<Object> merger = createDefaultMerger(this.mockReader);
        final Func0<Observable<Object>> function = merger.toFunction();

        // when:
        final Observable<Object> mergeResults = merger.merge();
        verify(this.mockReader).read();
        final Observable<Object> functionResults = function.call();
        verify(this.mockReader, times(2)).read();

        // then:
        assertObservablesEqual(mergeResults, functionResults);
    }

    protected abstract ResourceMerger<Object> createDefaultMerger(ReadableResource<Object> reader);

}
