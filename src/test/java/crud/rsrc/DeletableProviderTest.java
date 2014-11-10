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

import crud.spi.DeletableProviderSpec;
import crud.spi.DeletableSpec;
import crud.spi.Resource;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link DeletableProvider} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class DeletableProviderTest {

    protected final DeletableSpec<Object> mockResource = mock(DeletableSpec.class);

    protected final DeletableProviderSpec<Object, Object> mockProvider = mock(DeletableProviderSpec.class);


    @Before
    public void setup() {
        when(this.mockResource.delete()).thenReturn(Observable.empty());
        when(this.mockProvider.deleter(any())).thenReturn(this.mockResource);
        when(this.mockProvider.deleter(null)).thenThrow(new NullPointerException("mock"));
    }

    @Test
    public void getDefaultKeyReturnsNonNullResource() {
        // given:
        final DeletableProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource = provider.deleter(key);

        // then:
        assertNotNull(resource);
    }

    @Test
    public void twoResourcesFromSameKeyAreEqual() {
        // given:
        final DeletableProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource1 = provider.deleter(key);
        final Resource resource2 = provider.deleter(key);

        // then:
        assertEquals(resource1, resource2);
    }

    @Test(expected=NullPointerException.class)
    public void getNullKeyThrows() {
        // given:
        final DeletableProvider<Object, Object> provider = createDefaultProvider();

        // when:
        provider.deleter(null);
    }

    @Test
    public void providerNotEqualDelegate() {
        // given:
        final DeletableProvider<Object, Object> provider = createDefaultProvider();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockProvider, provider);
        assertNotEquals(provider, this.mockProvider);
    }

    @Test
    public void fromProviderReturnsSameObject() {
        // given:
        final DeletableProvider<Object, Object> origProvider = createDefaultProvider();

        // when:
        final DeletableProvider<Object, Object> wrappedProvider = DeletableProvider.from(
                origProvider);

        // then:
        assertSame(origProvider, wrappedProvider);
    }

    @Test
    public void providerCallsDelegate() {
        // given:
        final DeletableProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        provider.deleter(key);

        // then:
        verify(this.mockProvider).deleter(key);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final DeletableProvider<Object, Object> provider = createDefaultProvider();
        final Func1<Object, Deletable<Object>> function = provider.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key);

        // then:
        verify(this.mockProvider).deleter(key);
    }

    protected DeletableProvider<Object, Object> createDefaultProvider() {
        return DeletableProvider.from(this.mockProvider);
    }

    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
