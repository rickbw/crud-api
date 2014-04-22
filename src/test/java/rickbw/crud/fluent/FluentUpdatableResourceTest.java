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
package rickbw.crud.fluent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import rickbw.crud.UpdatableResource;
import rickbw.crud.UpdatableResourceTest;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link FluentUpdatableResource} that don't require
 * wrapping the delegate in an additional layer of nested subclasses. Those
 * layered behaviors (like transformations) are covered in test classes of
 * their own.
 */
public class FluentUpdatableResourceTest extends UpdatableResourceTest<Object, Object> {

    @SuppressWarnings("unchecked")
    protected final UpdatableResource<Object, Object> mockDelegate = mock(UpdatableResource.class);


    @Before
    public void setup() {
        when(this.mockDelegate.update(any())).thenReturn(Observable.empty());
    }

    @Test
    public void fluentResourceNotEqualDelegate() {
        // given:
        final FluentUpdatableResource<Object, Object> resource = createDefaultResource();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockDelegate, resource);
        assertNotEquals(resource, this.mockDelegate);
    }

    @Test
    public void fluentResourceCallsDelegate() {
        // given:
        final FluentUpdatableResource<Object, Object> resource = createDefaultResource();
        final Object update = createDefaultUpdate();

        // when:
        resource.update(update);

        // then:
        verify(this.mockDelegate).update(update);
    }

    @Test
    public void fromFluentResourceReturnsSameObject() {
        // given:
        final FluentUpdatableResource<Object, Object> origRsrc = createDefaultResource();

        // when:
        final FluentUpdatableResource<Object, Object> wrappedRsrc = FluentUpdatableResource.from(origRsrc);

        // then:
        assertSame(origRsrc, wrappedRsrc);
    }

    @Test
    public void equalResourcesHaveEqualFunctions() {
        // given:
        final FluentUpdatableResource<Object, Object> resource1 = createDefaultResource();
        final FluentUpdatableResource<Object, Object> resource2 = createDefaultResource();

        // when:
        final Func1<Object, Observable<Object>> function1 = resource1.toFunction();
        final Func1<Object, Observable<Object>> function2 = resource2.toFunction();

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
        final FluentUpdatableResource<Object, Object> resource1 = createDefaultResource();
        final FluentUpdatableResource<Object, Object> resource2 = createDefaultResource();
        final Func1<Object, Observable<Object>> function1 = resource1.toFunction();
        final Func1<Object, Observable<Object>> function2 = resource2.toFunction();

        // when:
        final String string1 = function1.toString();
        final String string2 = function2.toString();

        // then:
        assertEquals(string1, string2);
    }

    @Test
    public void equalFunctionsHaveEqualHashcodes() {
        // given:
        final FluentUpdatableResource<Object, Object> resource1 = createDefaultResource();
        final FluentUpdatableResource<Object, Object> resource2 = createDefaultResource();
        final Func1<Object, Observable<Object>> function1 = resource1.toFunction();
        final Func1<Object, Observable<Object>> function2 = resource2.toFunction();

        // when:
        final int hash1 = function1.hashCode();
        final int hash2 = function2.hashCode();

        // then:
        assertEquals(hash1, hash2);
    }

    @Override
    protected FluentUpdatableResource<Object, Object> createDefaultResource() {
        return FluentUpdatableResource.from(this.mockDelegate);
    }

    @Override
    protected Object createDefaultUpdate() {
        return "Hello, World";
    }

}
