/* Copyright 2013–2014 Rick Warren
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
package crud.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;


/**
 * A base class for all unit tests for {@link ResourceProvider}s.
 */
public abstract class ResourceProviderTest<KEY> {

    @Test
    public void getDefaultKeyReturnsNonNullResource() {
        // given:
        final ResourceProvider<KEY> provider = createDefaultProvider();
        final KEY key = createDefaultKey();

        // when:
        final Resource resource = provider.get(key);

        // then:
        assertNotNull(resource);
    }

    @Test
    public void twoResourcesFromSameKeyAreEqual() {
        // given:
        final ResourceProvider<KEY> provider = createDefaultProvider();
        final KEY key = createDefaultKey();

        // when:
        final Resource resource1 = provider.get(key);
        final Resource resource2 = provider.get(key);

        // then:
        assertEquals(resource1, resource2);
    }

    @Test(expected=NullPointerException.class)
    public void getNullKeyThrows() {
        // given:
        final ResourceProvider<KEY> provider = createDefaultProvider();

        // when:
        provider.get(null);
    }

    /**
     * Create and return a new instance of the {@link ResourceProvider} class
     * under test.
     */
    protected abstract ResourceProvider<KEY> createDefaultProvider();

    /**
     * Create and return a key that can be passed to the providers returned by
     * {@link #createDefaultProvider()}.
     */
    protected abstract KEY createDefaultKey();

}
