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
package rickbw.crud;

import static rickbw.crud.RxAssertions.assertObservablesEqual;

import org.junit.Test;

import rx.Observable;


/**
 * A base class for all unit test classes that operate on
 * {@link DeletableResource}s. It tests basic contracts that should hold true
 * for all implementations.
 */
public abstract class DeletableResourceTest<RESP> extends ResourceTest {

    /**
     * This base class can't test true idempotence, because it can't observe
     * the state that was deleted. But it can evaluate whether the responses
     * are the same, which they should be if the deletes were indeed
     * idempotent.
     */
    @Test
    public void responsesFromRepeatedDeletedAreTheSame() {
        // given:
        final DeletableResource<RESP> resource = createDefaultResource();

        // when:
        final Observable<RESP> result1 = resource.delete();
        final Observable<RESP> result2 = resource.delete();

        // then:
        assertObservablesEqual(result1, result2);
    }

    @Override
    protected abstract DeletableResource<RESP> createDefaultResource();

}
