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
package crud.fluent;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.ResourceProviderTest;
import crud.core.DeletableResource;
import crud.core.DeletableResourceProvider;
import rx.Observable;
import rx.functions.Func1;


/**
 * Tests those methods of {@link FluentDeletableResourceProvider} that don't
 * require wrapping the delegate in an additional layer of nested subclasses.
 * Those layered behaviors (like retries) are covered in test classes of their
 * own.
 */
public class FluentDeletableResourceProviderTest extends ResourceProviderTest<Object> {

    @SuppressWarnings("unchecked")
    protected final DeletableResource<Object> mockResource = mock(DeletableResource.class);

    @SuppressWarnings("unchecked")
    protected final DeletableResourceProvider<Object, Object> mockProvider = mock(DeletableResourceProvider.class);


    @Before
    public void setup() {
        when(this.mockResource.delete()).thenReturn(Observable.empty());
        when(this.mockProvider.get(any())).thenReturn(this.mockResource);
        when(this.mockProvider.get(null)).thenThrow(new NullPointerException("mock"));
    }

    @Test
    public void fluentProviderNotEqualDelegate() {
        // given:
        final FluentDeletableResourceProvider<Object, Object> provider = createDefaultProvider();

        // then:
        // Don't know which object's equals() gets called, so check both:
        assertNotEquals(this.mockProvider, provider);
        assertNotEquals(provider, this.mockProvider);
    }

    @Test
    public void fromFluentProviderReturnsSameObject() {
        // given:
        final FluentDeletableResourceProvider<Object, Object> origProvider = createDefaultProvider();

        // when:
        final FluentDeletableResourceProvider<Object, Object> wrappedProvider = FluentDeletableResourceProvider.from(
                origProvider);

        // then:
        assertSame(origProvider, wrappedProvider);
    }

    @Test
    public void fluentProviderCallsDelegate() {
        // given:
        final FluentDeletableResourceProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();

        // when:
        provider.get(key);

        // then:
        verify(this.mockProvider).get(key);
    }

    @Test
    public void functionCallsDelegate() {
        // given:
        final FluentDeletableResourceProvider<Object, Object> provider = createDefaultProvider();
        final Func1<Object, FluentDeletableResource<Object>> function = provider.toFunction();
        final Object key = createDefaultKey();

        // when:
        function.call(key);

        // then:
        verify(this.mockProvider).get(key);
    }

    @Override
    protected FluentDeletableResourceProvider<Object, Object> createDefaultProvider() {
        return FluentDeletableResourceProvider.from(this.mockProvider);
    }

    @Override
    protected Object createDefaultKey() {
        return "Hello, World";
    }

}
