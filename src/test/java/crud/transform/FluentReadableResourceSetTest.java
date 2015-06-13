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
package crud.transform;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.ReadableResource;
import crud.core.ReadableResourceSet;
import crud.core.ResourceSetTest;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link FluentReadableResourceSet} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class FluentReadableResourceSetTest extends ResourceSetTest<Object> {

    protected final ReadableResource<Object> mockResource = mock(ReadableResource.class);
    protected final ReadableResourceSet<Object, Object> mockResourceSet = mock(ReadableResourceSet.class);


    @Before
    public void setup() {
        when(this.mockResource.read()).thenReturn(Observable.empty());
        when(this.mockResourceSet.get(any())).thenReturn(this.mockResource);
        when(this.mockResourceSet.get(null)).thenThrow(new NullPointerException("mock"));
    }

    @Test
    public void fluentResourceSetNotEqualDelegate() {
        // given:
        final FluentReadableResourceSet<Object, Object> rsrcSet = createDefaultResourceSet();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockResourceSet, rsrcSet);
        assertNotEquals(rsrcSet, this.mockResourceSet);
    }

    @Test
    public void fromFluentResourceSetReturnsSameObject() {
        // given:
        final FluentReadableResourceSet<Object, Object> origRsrcSet = createDefaultResourceSet();

        // when:
        final FluentReadableResourceSet<Object, Object> wrappedRsrcSet = FluentReadableResourceSet.from(
                origRsrcSet);

        // then:
        assertSame(origRsrcSet, wrappedRsrcSet);
    }

    @Test
    public void fluentResourceSetCallsDelegate() {
        // given:
        final FluentReadableResourceSet<Object, Object> rsrcSet = createDefaultResourceSet();
        final Object key = createDefaultKey();

        // when:
        rsrcSet.get(key);

        // then:
        verify(this.mockResourceSet).get(key);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final FluentReadableResourceSet<Object, Object> rsrcSet = createDefaultResourceSet();
        final Func1<Object, FluentReadableResource<Object>> function = rsrcSet.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key);

        // then:
        verify(this.mockResourceSet).get(key);
    }

    @Override
    protected FluentReadableResourceSet<Object, Object> createDefaultResourceSet() {
        return FluentReadableResourceSet.from(this.mockResourceSet);
    }

    @Override
    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
