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
package crud.spi;

import static crud.RxAssertions.assertObservablesEqual;

import org.junit.Test;

import rx.Observable;


/**
 * A base class for all unit test classes that operate on
 * {@link SettableSpec}s. It tests basic contracts that should hold true
 * for all implementations.
 */
public abstract class WritableSpecTest<RSRC, RESP> extends ResourceTest {

    /**
     * This base class can't test true idempotence, because it can't observe
     * the state that was written. But it can evaluate whether the responses
     * are the same, which they should be if the writes were indeed
     * idempotent.
     */
    @Test
    public void responsesFromRepeatedWritesAreTheSame() {
        // given:
        final SettableSpec<RSRC, RESP> resource = createDefaultResource();
        final Observable<RSRC> newValue = createDefaultResourceState();

        // when:
        final Observable<RESP> result1 = resource.set(newValue);
        final Observable<RESP> result2 = resource.set(newValue);

        // then:
        assertObservablesEqual(result1, result2);
    }

    @Override
    protected abstract SettableSpec<RSRC, RESP> createDefaultResource();

    /**
     * Create an object suitable for being written to the resources returned
     * by {@link #createDefaultResource()}.
     */
    protected abstract Observable<RSRC> createDefaultResourceState();

}
