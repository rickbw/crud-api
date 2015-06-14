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

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.ReadableResource;
import crud.core.ReadableResourceSet;
import crud.core.ResourceSetTest;
import crud.core.Session;
import rx.Observable;
import rx.functions.Func2;


/**
 * Tests those methods of {@link TransformedReadableResourceSet} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class TransformedReadableResourceSetTest extends ResourceSetTest<Object> {

    protected final ReadableResource<Object> mockResource = mock(ReadableResource.class);
    protected final ReadableResourceSet<Object, Object> mockResourceSet = mock(ReadableResourceSet.class);


    @Before
    public void setup() {
        when(this.mockResource.read()).thenReturn(Observable.empty());
        when(this.mockResourceSet.get(any(), eq(this.mockSession))).thenReturn(this.mockResource);
    }

    @Test
    public void fromTransformedResourceSetReturnsSameObject() {
        // given:
        final TransformedReadableResourceSet<Object, Object> origRsrcSet = createDefaultResourceSet();

        // when:
        final TransformedReadableResourceSet<Object, Object> wrappedRsrcSet = TransformedReadableResourceSet.from(
                origRsrcSet);

        // then:
        assertSame(origRsrcSet, wrappedRsrcSet);
    }

    @Test
    public void transformedResourceSetCallsDelegate() {
        // given:
        final TransformedReadableResourceSet<Object, Object> rsrcSet = createDefaultResourceSet();
        final Object key = createDefaultKey();

        // when:
        rsrcSet.get(key, this.mockSession);

        // then:
        verify(this.mockResourceSet).get(key, this.mockSession);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final TransformedReadableResourceSet<Object, Object> rsrcSet = createDefaultResourceSet();
        final Func2<Object, Session, TransformedReadableResource<Object>> function = rsrcSet.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key, this.mockSession);

        // then:
        verify(this.mockResourceSet).get(key, this.mockSession);
    }

    @Override
    protected TransformedReadableResourceSet<Object, Object> createDefaultResourceSet() {
        return TransformedReadableResourceSet.from(this.mockResourceSet);
    }

    @Override
    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
