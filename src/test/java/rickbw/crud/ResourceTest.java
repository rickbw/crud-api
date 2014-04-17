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
package rickbw.crud;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * A base class for all unit test classes that operate on
 * {@link Resource}s. It tests basic contracts that should hold true
 * for all implementations.
 */
public abstract class ResourceTest {

    @Test
    public void twoDefaultInstancesAreEqual() {
        // given:
        final Resource resource1 = createDefaultResource();
        final Resource resource2 = createDefaultResource();

        // then:
        assertEquals(resource1, resource2);
    }

    @Test
    public void twoDefaultInstancesHaveSameHashcode() {
        // given:
        final Resource resource1 = createDefaultResource();
        final Resource resource2 = createDefaultResource();

        // then:
        assertEquals(resource1.hashCode(), resource2.hashCode());
    }

    /**
     * Create a new instance of the resource.
     */
    protected abstract Resource createDefaultResource();

}
