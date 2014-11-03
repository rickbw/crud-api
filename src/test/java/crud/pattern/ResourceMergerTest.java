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

import static crud.RxAssertions.assertObservablesEqual;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.ReadableResource;
import rx.Observable;
import rx.functions.Func0;


public abstract class ResourceMergerTest {

    protected final ReadableResource<Object> mockReader = mock(ReadableResource.class);
    protected final Observable<Object> mockReaderState = Observable.<Object>just("Hello, World");


    @Before
    public void setup() {
        when(this.mockReader.get()).thenReturn(mockReaderState);
    }

    @Test
    public void twoDefaultMergersAreEqual() {
        // given:
        final ResourceMerger<Object> merger1 = createDefaultMerger(this.mockReader);
        final ResourceMerger<Object> merger2 = createDefaultMerger(this.mockReader);

        // then:
        assertEquals(merger1, merger2);
    }

    @Test
    public void hashCodesOfEqualMergersAreEqual() {
        // given:
        final ResourceMerger<Object> merger1 = createDefaultMerger(this.mockReader);
        final ResourceMerger<Object> merger2 = createDefaultMerger(this.mockReader);

        // then:
        assertEquals(merger1.hashCode(), merger2.hashCode());
    }

    @Test
    public void toStringResultsOfEqualMergersAreEqual() {
        // given:
        final ResourceMerger<Object> merger1 = createDefaultMerger(this.mockReader);
        final ResourceMerger<Object> merger2 = createDefaultMerger(this.mockReader);

        // then:
        assertEquals(merger1.toString(), merger2.toString());
    }

    @Test
    public void functionsOfEqualMergersAreEqual() {
        // given:
        final ResourceMerger<Object> merger1 = createDefaultMerger(this.mockReader);
        final ResourceMerger<Object> merger2 = createDefaultMerger(this.mockReader);

        // then:
        assertEquals(merger1.toFunction(), merger2.toFunction());
    }

    @Test
    public void functionsOfEqualMergersHaveEqualHashCodes() {
        // given:
        final ResourceMerger<Object> merger1 = createDefaultMerger(this.mockReader);
        final ResourceMerger<Object> merger2 = createDefaultMerger(this.mockReader);
        final Func0<Observable<Object>> function1 = merger1.toFunction();
        final Func0<Observable<Object>> function2 = merger2.toFunction();

        // then:
        assertEquals(function1.hashCode(), function2.hashCode());
    }

    @Test
    public void functionsOfEqualMergersHaveEqualStrings() {
        // given:
        final ResourceMerger<Object> merger1 = createDefaultMerger(this.mockReader);
        final ResourceMerger<Object> merger2 = createDefaultMerger(this.mockReader);
        final Func0<Observable<Object>> function1 = merger1.toFunction();
        final Func0<Observable<Object>> function2 = merger2.toFunction();

        // then:
        assertEquals(function1.toString(), function2.toString());
    }

    @Test
    public void functionBehavesLikeMerge() {
        // given:
        final ResourceMerger<Object> merger = createDefaultMerger(this.mockReader);
        final Func0<Observable<Object>> function = merger.toFunction();

        // when:
        final Observable<Object> mergeResults = merger.merge();
        verify(this.mockReader).get();
        final Observable<Object> functionResults = function.call();
        verify(this.mockReader, times(2)).get();

        // then:
        assertObservablesEqual(mergeResults, functionResults);
    }

    protected abstract ResourceMerger<Object> createDefaultMerger(ReadableResource<Object> reader);

}
