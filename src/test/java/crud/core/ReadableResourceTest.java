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
package crud.core;

import static crud.core.RxAssertions.assertObservablesEqual;

import org.junit.Test;

import rx.Observable;


/**
 * A base class for all unit test classes that operate on
 * {@link ReadableResource}s. It tests basic contracts that should hold true
 * for all implementations.
 */
public abstract class ReadableResourceTest<RSRC> extends ResourceTest {

    @Test
    public void getsAreIdempotent() {
        // given:
        final ReadableResource<RSRC> resource = createDefaultResource();

        // when:
        final Observable<RSRC> result1 = resource.get();
        final Observable<RSRC> result2 = resource.get();

        // then:
        assertObservablesEqual(result1, result2);
    }

    @Override
    protected abstract ReadableResource<RSRC> createDefaultResource();

}
