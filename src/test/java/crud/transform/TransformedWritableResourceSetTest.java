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
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.ResourceSetTest;
import crud.core.Session;
import crud.core.WritableResource;
import crud.core.WritableResourceSet;
import rx.Observable;
import rx.functions.Func2;


/**
 * Tests those methods of {@link TransformedWritableResourceSet} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class TransformedWritableResourceSetTest extends ResourceSetTest<Object> {

    protected final WritableResource<Object, Object> mockResource = mock(WritableResource.class);
    protected final WritableResourceSet<Object, Object, Object> mockResourceSet = mock(WritableResourceSet.class);


    @Before
    public void setup() {
        when(this.mockResource.write(any())).thenReturn(Observable.empty());
        when(this.mockResourceSet.get(any(), eq(this.mockSession))).thenReturn(this.mockResource);
    }

    @Test
    public void transformedResourceSetNotEqualDelegate() {
        // given:
        final TransformedWritableResourceSet<Object, Object, Object> rsrcSet = createDefaultResourceSet();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockResourceSet, rsrcSet);
        assertNotEquals(rsrcSet, this.mockResourceSet);
    }

    @Test
    public void fromTransformedResourceSetReturnsSameObject() {
        // given:
        final TransformedWritableResourceSet<Object, Object, Object> origRsrcSet = createDefaultResourceSet();

        // when:
        final TransformedWritableResourceSet<Object, Object, Object> wrappedRsrcSet = TransformedWritableResourceSet.from(
                origRsrcSet);

        // then:
        assertSame(origRsrcSet, wrappedRsrcSet);
    }

    @Test
    public void transformedResourceSetCallsDelegate() {
        // given:
        final TransformedWritableResourceSet<Object, Object, Object> rsrcSet = createDefaultResourceSet();
        final Object key = createDefaultKey();

        // when:
        rsrcSet.get(key, this.mockSession);

        // then:
        verify(this.mockResourceSet).get(key, this.mockSession);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final TransformedWritableResourceSet<Object, Object, Object> rsrcSet = createDefaultResourceSet();
        final Func2<Object, Session, TransformedWritableResource<Object, Object>> function = rsrcSet.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key, this.mockSession);

        // then:
        verify(this.mockResourceSet).get(key, this.mockSession);
    }

    @Override
    protected TransformedWritableResourceSet<Object, Object, Object> createDefaultResourceSet() {
        return TransformedWritableResourceSet.from(this.mockResourceSet);
    }

    @Override
    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
