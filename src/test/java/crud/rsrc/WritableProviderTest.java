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
import crud.spi.WritableProviderSpec;
import crud.spi.WritableSpec;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link WritableProvider} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class WritableProviderTest {

    protected final WritableSpec<Object, Object> mockResource = mock(WritableSpec.class);

    protected final WritableProviderSpec<Object, Object, Object> mockProvider = mock(WritableProviderSpec.class);


    @Before
    public void setup() {
        when(this.mockResource.write(any())).thenReturn(Observable.empty());
        when(this.mockProvider.writer(any())).thenReturn(this.mockResource);
        when(this.mockProvider.writer(null)).thenThrow(new NullPointerException("mock"));
    }

    @Test
    public void getDefaultKeyReturnsNonNullResource() {
        // given:
        final WritableProvider<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource = provider.writer(key);

        // then:
        assertNotNull(resource);
    }

    @Test
    public void twoResourcesFromSameKeyAreEqual() {
        // given:
        final WritableProvider<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        final Resource resource1 = provider.writer(key);
        final Resource resource2 = provider.writer(key);

        // then:
        assertEquals(resource1, resource2);
    }

    @Test(expected=NullPointerException.class)
    public void getNullKeyThrows() {
        // given:
        final WritableProvider<Object, Object, Object> provider = createDefaultProvider();

        // when:
        provider.writer(null);
    }

    @Test
    public void fluentProviderNotEqualDelegate() {
        // given:
        final WritableProvider<Object, Object, Object> provider = createDefaultProvider();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockProvider, provider);
        assertNotEquals(provider, this.mockProvider);
    }

    @Test
    public void fromFluentProviderReturnsSameObject() {
        // given:
        final WritableProvider<Object, Object, Object> origProvider = createDefaultProvider();

        // when:
        final WritableProvider<Object, Object, Object> wrappedProvider = WritableProvider.from(
                origProvider);

        // then:
        assertSame(origProvider, wrappedProvider);
    }

    @Test
    public void fluentProviderCallsDelegate() {
        // given:
        final WritableProvider<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        provider.writer(key);

        // then:
        verify(this.mockProvider).writer(key);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final WritableProvider<Object, Object, Object> provider = createDefaultProvider();
        final Func1<Object, Writable<Object, Object>> function = provider.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key);

        // then:
        verify(this.mockProvider).writer(key);
    }

    protected WritableProvider<Object, Object, Object> createDefaultProvider() {
        return WritableProvider.from(this.mockProvider);
    }

    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
