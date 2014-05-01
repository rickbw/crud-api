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
package rickbw.crud.fluent;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;


/**
 * Tests the nested subclass of {@link FluentWritableResource} that handles
 * transforming responses.
 */
public class FluentWritableResourceMapResponseTest extends FluentWritableResourceTest {

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
        final FluentWritableResource<Object, Object> resource = createDefaultResource();
        final Object newValue = createDefaultResourceState();
        final String origResponse = "world";
        final String mappedResponse = mapper.call(origResponse);

        // when:
        when(super.mockDelegate.write(newValue)).thenReturn(Observable.<Object>from(origResponse));
        final Observable<Object> response = resource.write(newValue);

        // then:
        final Object responseValue = response.toBlockingObservable().first();
        assertEquals(mappedResponse, responseValue);
    }

    @Override
    protected FluentWritableResource<Object, Object> createDefaultResource() {
        return super.createDefaultResource().<Object>mapResponse(mapper);
    }

}