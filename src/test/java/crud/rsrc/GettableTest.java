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
package crud.rsrc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Supplier;

import crud.spi.GettableSpec;
import crud.spi.GettableSpecTest;
import rx.Observable;
import rx.functions.Func0;


/**
 * Tests those methods of {@link Gettable} that don't require
 * wrapping the delegate in an additional layer of nested subclasses. Those
 * layered behaviors (like retries) are covered in test classes of their own.
 */
public class GettableTest extends GettableSpecTest<Object> {

    protected final GettableSpec<Object> mockDelegate = mock(GettableSpec.class);
    /**
     * Mockito considers Supplier.get() and GettableSpec.get() to be
     * different methods, even though the latter overrides the former.
     * Changing the static type of the target works around this stupidity.
     */
    private final Supplier<Observable<Object>> mockSupplier = this.mockDelegate;


    @Before
    public void setup() {
        when(this.mockDelegate.get()).thenReturn(Observable.empty());
        when(this.mockSupplier.get()).thenReturn(Observable.empty());
    }

    @Test
    public void resourceNotEqualDelegate() {
        // given:
        final Gettable<Object> resource = createDefaultResource();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockDelegate, resource);
        assertNotEquals(resource, this.mockDelegate);
    }

    @Test
    public void resourceCallsDelegate() {
        // given:
        final Gettable<Object> resource = createDefaultResource();

        // when:
        resource.get();

        // then:
        verify(this.mockSupplier).get();
    }

    @Test
    public void fromResourceReturnsSameObject() {
        // given:
        final Gettable<Object> origRsrc = createDefaultResource();

        // when:
        final Gettable<Object> wrappedRsrc = Gettable.from(origRsrc);

        // then:
        assertSame(origRsrc, wrappedRsrc);
    }

    @Test
    public void equalResourcesHaveEqualFunctions() {
        // given:
        final Gettable<Object> resource1 = createDefaultResource();
        final Gettable<Object> resource2 = createDefaultResource();

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
        final Gettable<Object> resource1 = createDefaultResource();
        final Gettable<Object> resource2 = createDefaultResource();
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
        final Gettable<Object> resource1 = createDefaultResource();
        final Gettable<Object> resource2 = createDefaultResource();
        final Func0<Observable<Object>> function1 = resource1.toFunction();
        final Func0<Observable<Object>> function2 = resource2.toFunction();

        // when:
        final int hash1 = function1.hashCode();
        final int hash2 = function2.hashCode();

        // then:
        assertEquals(hash1, hash2);
    }

    @Override
    protected Gettable<Object> createDefaultResource() {
        return Gettable.from(this.mockDelegate);
    }

}
