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

import crud.spi.ReadableProviderSpec;
import crud.spi.ReadableSpec;
import crud.spi.Resource;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link ReadableProvider} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class ReadableProviderTest {

    protected final ReadableSpec<Object> mockResource = mock(ReadableSpec.class);

    protected final ReadableProviderSpec<Object, Object> mockProvider = mock(ReadableProviderSpec.class);


    @Before
    public void setup() {
        when(this.mockResource.get()).thenReturn(Observable.empty());
        when(this.mockProvider.reader(any())).thenReturn(this.mockResource);
        when(this.mockProvider.reader(null)).thenThrow(new NullPointerException("mock"));
    }

    @Test
    public void getDefaultKeyReturnsNonNullResource() {
        // given:
        final ReadableProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final ReadableSpec<Object> resource = provider.reader(key);

        // then:
        assertNotNull(resource);
    }

    @Test
    public void twoResourcesFromSameKeyAreEqual() {
        // given:
        final ReadableProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource1 = provider.reader(key);
        final Resource resource2 = provider.reader(key);

        // then:
        assertEquals(resource1, resource2);
    }

    @Test(expected=NullPointerException.class)
    public void getNullKeyThrows() {
        // given:
        final ReadableProvider<Object, Object> provider = createDefaultProvider();

        // when:
        provider.reader(null);
    }

    @Test
    public void fluentProviderNotEqualDelegate() {
        // given:
        final ReadableProvider<Object, Object> provider = createDefaultProvider();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockProvider, provider);
        assertNotEquals(provider, this.mockProvider);
    }

    @Test
    public void fromFluentProviderReturnsSameObject() {
        // given:
        final ReadableProvider<Object, Object> origProvider = createDefaultProvider();

        // when:
        final ReadableProvider<Object, Object> wrappedProvider = ReadableProvider.from(
                origProvider);

        // then:
        assertSame(origProvider, wrappedProvider);
    }

    @Test
    public void fluentProviderCallsDelegate() {
        // given:
        final ReadableProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        provider.reader(key);

        // then:
        verify(this.mockProvider).reader(key);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final ReadableProvider<Object, Object> provider = createDefaultProvider();
        final Func1<Object, Readable<Object>> function = provider.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key);

        // then:
        verify(this.mockProvider).reader(key);
    }

    protected ReadableProvider<Object, Object> createDefaultProvider() {
        return ReadableProvider.from(this.mockProvider);
    }

    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
