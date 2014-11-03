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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;


/**
 * Tests the nested subclass of {@link FluentUpdatableResource} that handles
 * transforming responses.
 */
public class FluentUpdatableResourceMapResponseTest extends FluentUpdatableResourceTest {

    private static final String RESPONSE_PREFIX = "Goodbye, cruel ";

    private static final Func1<Object, String> mapper = new Func1<Object, String>() {
        @Override
        public String call(final Object input) {
            return RESPONSE_PREFIX + input;
        }
    };


    @Test
    public void transformationApplied() {
        // given:
        final FluentUpdatableResource<Object, Object> resource = createDefaultResource();
        final Object update = createDefaultUpdate();

        // when:
        when(super.mockDelegate.update(update)).thenReturn(Observable.<Object>just("world"));
        final Observable<Object> response = resource.update(update);

        // then:
        final Object responseValue = response.toBlocking().first();
        assertTrue(((String) responseValue).startsWith(RESPONSE_PREFIX));
    }

    @Override
    protected FluentUpdatableResource<Object, Object> createDefaultResource() {
        return super.createDefaultResource().<Object>mapResponse(mapper);
    }

}
