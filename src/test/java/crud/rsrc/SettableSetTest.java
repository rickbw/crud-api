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

import crud.spi.Resource;
import crud.spi.SettableSetSpec;
import crud.spi.SettableSpec;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link SettableSet} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class SettableSetTest {

    protected final SettableSpec<Object, Object> mockResource = mock(SettableSpec.class);

    protected final SettableSetSpec<Object, Object, Object> mockProvider = mock(SettableSetSpec.class);


    @Before
    public void setup() {
        when(this.mockResource.set(any(Observable.class))).thenReturn(Observable.empty());
        when(this.mockProvider.setter(any())).thenReturn(this.mockResource);
        when(this.mockProvider.setter(null)).thenThrow(new NullPointerException("mock"));
    }

    @Test
    public void getDefaultKeyReturnsNonNullResource() {
        // given:
        final SettableSet<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource = provider.setter(key);

        // then:
        assertNotNull(resource);
    }

    @Test
    public void twoResourcesFromSameKeyAreEqual() {
        // given:
        final SettableSet<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource1 = provider.setter(key);
        final Resource resource2 = provider.setter(key);

        // then:
        assertEquals(resource1, resource2);
    }

    @Test(expected=NullPointerException.class)
    public void getNullKeyThrows() {
        // given:
        final SettableSet<Object, Object, Object> provider = createDefaultProvider();

        // when:
        provider.setter(null);
    }

    @Test
    public void providerNotEqualDelegate() {
        // given:
        final SettableSet<Object, Object, Object> provider = createDefaultProvider();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockProvider, provider);
        assertNotEquals(provider, this.mockProvider);
    }

    @Test
    public void fromProviderReturnsSameObject() {
        // given:
        final SettableSet<Object, Object, Object> origProvider = createDefaultProvider();

        // when:
        final SettableSet<Object, Object, Object> wrappedProvider = SettableSet.from(
                origProvider);

        // then:
        assertSame(origProvider, wrappedProvider);
    }

    @Test
    public void providerCallsDelegate() {
        // given:
        final SettableSet<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        provider.setter(key);

        // then:
        verify(this.mockProvider).setter(key);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final SettableSet<Object, Object, Object> provider = createDefaultProvider();
        final Func1<Object, Settable<Object, Object>> function = provider.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key);

        // then:
        verify(this.mockProvider).setter(key);
    }

    protected SettableSet<Object, Object, Object> createDefaultProvider() {
        return SettableSet.from(this.mockProvider);
    }

    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
