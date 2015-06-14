/* Copyright 2014–2015 Rick Warren
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
package crud.transform;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;


/**
 * Tests the nested subclass of {@link TransformedWritableResource} that handles
 * transforming responses.
 */
public class TransformedWritableResourceMapResponseTest extends TransformedWritableResourceTest {

    private static final String RESPONSE_PREFIX = "Goodbye, cruel ";

    private static final Func1<Object, Object> singleMapper = new Func1<Object, Object>() {
        @Override
        public String call(final Object input) {
            return RESPONSE_PREFIX + input;
        }
    };
    private static final Func1<Observable<Object>, Observable<Object>> mapper = new Func1<Observable<Object>, Observable<Object>>() {
        @Override
        public Observable<Object> call(final Observable<Object> input) {
            return input.map(singleMapper);
        }
    };


    @Test
    public void transformationApplied() {
        // given:
        final TransformedWritableResource<Object, Object> resource = createDefaultResource();
        final Object newValue = createDefaultResourceState();
        final String origResponse = "world";
        final String mappedResponse = singleMapper.call(origResponse).toString();

        // when:
        when(super.mockDelegate.write(newValue)).thenReturn(Observable.<Object>just(origResponse));
        final Observable<Object> response = resource.write(newValue);

        // then:
        final Object responseValue = response.toBlocking().first();
        assertEquals(mappedResponse, responseValue);
    }

    @Override
    protected TransformedWritableResource<Object, Object> createDefaultResource() {
        return super.createDefaultResource().mapResponse(mapper);
    }

}
