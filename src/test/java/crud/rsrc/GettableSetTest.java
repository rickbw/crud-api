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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.spi.GettableSetSpec;
import crud.spi.GettableSpec;
import crud.spi.Resource;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link GettableSet} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class GettableSetTest {

    protected final GettableSpec<Object> mockResource = mock(GettableSpec.class);

    protected final GettableSetSpec<Object, Object> mockProvider = mock(GettableSetSpec.class);


    @Before
    public void setup() {
        when(this.mockResource.get()).thenReturn(Observable.empty());
        when(this.mockProvider.getter(any())).thenReturn(this.mockResource);
        when(this.mockProvider.getter(null)).thenThrow(new NullPointerException("mock"));
    }

    @Test
    public void getDefaultKeyReturnsNonNullResource() {
        // given:
        final GettableSet<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final GettableSpec<Object> resource = provider.getter(key);

        // then:
        assertNotNull(resource);
    }

    @Test
    public void twoResourcesFromSameKeyAreEqual() {
        // given:
        final GettableSet<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource1 = provider.getter(key);
        final Resource resource2 = provider.getter(key);

        // then:
        assertEquals(resource1, resource2);
    }

    @Test(expected=NullPointerException.class)
    public void getNullKeyThrows() {
        // given:
        final GettableSet<Object, Object> provider = createDefaultProvider();

        // when:
        provider.getter(null);
    }

    @Test
    public void providerNotEqualDelegate() {
        // given:
        final GettableSet<Object, Object> provider = createDefaultProvider();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockProvider, provider);
        assertNotEquals(provider, this.mockProvider);
    }

    @Test
    public void fromProviderReturnsSameObject() {
        // given:
        final GettableSet<Object, Object> origProvider = createDefaultProvider();

        // when:
        final GettableSet<Object, Object> wrappedProvider = GettableSet.from(
                origProvider);

        // then:
        assertSame(origProvider, wrappedProvider);
    }

    @Test
    public void providerCallsDelegate() {
        // given:
        final GettableSet<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        provider.getter(key);

        // then:
        verify(this.mockProvider).getter(key);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final GettableSet<Object, Object> provider = createDefaultProvider();
        final Func1<Object, Gettable<Object>> function = provider.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key);

        // then:
        verify(this.mockProvider).getter(key);
    }

    protected GettableSet<Object, Object> createDefaultProvider() {
        return GettableSet.from(this.mockProvider);
    }

    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
