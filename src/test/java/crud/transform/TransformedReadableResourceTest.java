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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Test;

import crud.core.ReadableResource;
import crud.core.ReadableResourceTest;
import rx.Observable;
import rx.functions.Func0;


/**
 * Tests those methods of {@link TransformedReadableResource} that don't require
 * wrapping the delegate in an additional layer of nested subclasses. Those
 * layered behaviors (like retries) are covered in test classes of their own.
 */
public class TransformedReadableResourceTest extends ReadableResourceTest<Object> {

    protected final ReadableResource<Object> mockDelegate = mock(ReadableResource.class);


    @Before
    public void setup() {
        when(this.mockDelegate.read()).thenReturn(Observable.empty());
    }

    @Test
    public void transformedResourceNotEqualDelegate() {
        // given:
        final TransformedReadableResource<Object> resource = createDefaultResource();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockDelegate, resource);
        assertNotEquals(resource, this.mockDelegate);
    }

    @Test
    public void transformedResourceCallsDelegate() {
        // given:
        final TransformedReadableResource<Object> resource = createDefaultResource();

        // when:
        resource.read();

        // then:
        verify(this.mockDelegate).read();
    }

    @Test
    public void fromTransformedResourceReturnsSameObject() {
        // given:
        final TransformedReadableResource<Object> origRsrc = createDefaultResource();

        // when:
        final TransformedReadableResource<Object> wrappedRsrc = TransformedReadableResource.from(origRsrc);

        // then:
        assertSame(origRsrc, wrappedRsrc);
    }

    @Test
    public void equalResourcesHaveEqualFunctions() {
        // given:
        final TransformedReadableResource<Object> resource1 = createDefaultResource();
        final TransformedReadableResource<Object> resource2 = createDefaultResource();

        // when:
        final Func0<Observable<Object>> function1 = resource1.toFunction();
        final Func0<Observable<Object>> function2 = resource2.toFunction();

        // then:
        // Resources should be equal, so functions too:
        assertEquals(function1, function2);
        // ...but functions should not be equal to resources:
        assertNotEquals(resource1, function1);
        assertNotEquals(resource2, function2);
    }

    @Test
    public void equalFunctionsHaveEqualStrings() {
        // given:
        final TransformedReadableResource<Object> resource1 = createDefaultResource();
        final TransformedReadableResource<Object> resource2 = createDefaultResource();
        final Func0<Observable<Object>> function1 = resource1.toFunction();
        final Func0<Observable<Object>> function2 = resource2.toFunction();

        // when:
        final String string1 = function1.toString();
        final String string2 = function2.toString();

        // then:
        assertEquals(string1, string2);
    }

    @Test
    public void equalFunctionsHaveEqualHashcodes() {
        // given:
        final TransformedReadableResource<Object> resource1 = createDefaultResource();
        final TransformedReadableResource<Object> resource2 = createDefaultResource();
        final Func0<Observable<Object>> function1 = resource1.toFunction();
        final Func0<Observable<Object>> function2 = resource2.toFunction();

        // when:
        final int hash1 = function1.hashCode();
        final int hash2 = function2.hashCode();

        // then:
        assertEquals(hash1, hash2);
    }

    @Test
    public void equalResourcesHaveEqualCallables() {
        // given:
        final TransformedReadableResource<Object> resource1 = createDefaultResource();
        final TransformedReadableResource<Object> resource2 = createDefaultResource();
        final Callable<Observable<Object>> callable1 = resource1.toCallable();
        final Callable<Observable<Object>> callable2 = resource2.toCallable();

        // then:
        // Resources should be equal, so Callables too:
        assertEquals(callable1, callable2);
        // ...but Callables should not be equal to resources:
        assertNotEquals(resource1, callable1);
        assertNotEquals(resource2, callable2);
    }

    @Test
    public void equalCallablesHaveEqualStrings() {
        // given:
        final TransformedReadableResource<Object> resource1 = createDefaultResource();
        final TransformedReadableResource<Object> resource2 = createDefaultResource();
        final Callable<Observable<Object>> callable1 = resource1.toCallable();
        final Callable<Observable<Object>> callable2 = resource2.toCallable();

        // when:
        final String string1 = callable1.toString();
        final String string2 = callable2.toString();

        // then:
        assertEquals(string1, string2);
    }

    @Test
    public void equalCallablesHaveEqualHashcodes() {
        // given:
        final TransformedReadableResource<Object> resource1 = createDefaultResource();
        final TransformedReadableResource<Object> resource2 = createDefaultResource();
        final Callable<Observable<Object>> callable1 = resource1.toCallable();
        final Callable<Observable<Object>> callable2 = resource2.toCallable();

        // when:
        final int hash1 = callable1.hashCode();
        final int hash2 = callable2.hashCode();

        // then:
        // Functions should be equal, so strings too:
        assertEquals(hash1, hash2);
    }

    @Override
    protected TransformedReadableResource<Object> createDefaultResource() {
        return TransformedReadableResource.from(this.mockDelegate);
    }

}
