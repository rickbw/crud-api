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
import crud.spi.UpdatableSetSpec;
import crud.spi.UpdatableSpec;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link UpdatableSet} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like transformations) are covered in test classes
 * of their own.
 */
public class UpdatableSetTest {

    protected final UpdatableSpec<Object, Object> mockResource = mock(UpdatableSpec.class);

    protected final UpdatableSetSpec<Object, Object, Object> mockProvider = mock(UpdatableSetSpec.class);


    @Before
    public void setup() {
        when(this.mockResource.update(any(Observable.class))).thenReturn(Observable.empty());
        when(this.mockProvider.updater(any())).thenReturn(this.mockResource);
        when(this.mockProvider.updater(null)).thenThrow(new NullPointerException("mock"));
    }

    @Test
    public void getDefaultKeyReturnsNonNullResource() {
        // given:
        final UpdatableSet<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource = provider.updater(key);

        // then:
        assertNotNull(resource);
    }

    @Test
    public void twoResourcesFromSameKeyAreEqual() {
        // given:
        final UpdatableSet<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource1 = provider.updater(key);
        final Resource resource2 = provider.updater(key);

        // then:
        assertEquals(resource1, resource2);
    }

    @Test(expected=NullPointerException.class)
    public void getNullKeyThrows() {
        // given:
        final UpdatableSet<Object, Object, Object> provider = createDefaultProvider();

        // when:
        provider.updater(null);
    }

    @Test
    public void providerNotEqualDelegate() {
        // given:
        final UpdatableSet<Object, Object, Object> provider = createDefaultProvider();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockProvider, provider);
        assertNotEquals(provider, this.mockProvider);
    }

    @Test
    public void fromProviderReturnsSameObject() {
        // given:
        final UpdatableSet<Object, Object, Object> origProvider = createDefaultProvider();

        // when:
        final UpdatableSet<Object, Object, Object> wrappedProvider = UpdatableSet.from(
                origProvider);

        // then:
        assertSame(origProvider, wrappedProvider);
    }

    @Test
    public void providerCallsDelegate() {
        // given:
        final UpdatableSet<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        provider.updater(key);

        // then:
        verify(this.mockProvider).updater(key);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final UpdatableSet<Object, Object, Object> provider = createDefaultProvider();
        final Func1<Object, Updatable<Object, Object>> function = provider.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key);

        // then:
        verify(this.mockProvider).updater(key);
    }

    protected UpdatableSet<Object, Object, Object> createDefaultProvider() {
        return UpdatableSet.from(this.mockProvider);
    }

    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
