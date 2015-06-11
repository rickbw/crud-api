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
package crud.pattern;

import static crud.core.RxAssertions.assertObservablesEqual;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import crud.core.ReadableResource;
import crud.core.UpdatableResource;
import rx.Observable;


public class ResourceMergerWithUpdaterTest extends ResourceMergerTest {

    private final UpdatableResource<Object, Object> mockUpdater = mock(UpdatableResource.class);
    private final Observable<Object> mockUpdaterResponse = Observable.<Object>just("Goodbye");


    @Override
    @Before
    public void setup() {
        super.setup();
        when(this.mockUpdater.update(any())).thenReturn(this.mockUpdaterResponse);
    }

    @Test
    public void mergeCallsUpdater() {
        // given:
        final ResourceMerger<Object> merger = createDefaultMerger(super.mockReader);

        // when:
        final Observable<Object> result = merger.merge();

        // then:
        assertObservablesEqual(this.mockUpdaterResponse, result);
        verify(this.mockUpdater).update(super.mockReaderState.toBlocking().single());
    }

    @Override
    protected ResourceMerger<Object> createDefaultMerger(final ReadableResource<Object> reader) {
        return ResourceMerger.withUpdater(reader, this.mockUpdater);
    }

}
