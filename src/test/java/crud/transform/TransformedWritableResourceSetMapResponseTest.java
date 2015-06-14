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


public class TransformedWritableResourceSetMapResponseTest
extends TransformedWritableResourceSetTest {

    private static final String PREFIX = "Goodbye, cruel ";

    private static final Func1<Object, Object> singleMapper = new Func1<Object, Object>() {
        @Override
        public Object call(final Object response) {
            return PREFIX + response;
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
        final TransformedWritableResourceSet<Object, Object, Object> rsrcSet = createDefaultResourceSet();
        final Object key = createDefaultKey();
        final String value = "Hello";
        final String origResponse = "world";
        final String mappedResponse = singleMapper.call(origResponse).toString();

        // when:
        when(super.mockResource.write(value)).thenReturn(Observable.<Object>just(origResponse));
        final TransformedWritableResource<Object, Object> resource = rsrcSet.get(key, this.mockSession);
        final Observable<Object> response = resource.write(value);

        // then:
        final String responseString = (String) response.toBlocking().single();
        assertEquals(mappedResponse, responseString);
    }

    @Override
    protected TransformedWritableResourceSet<Object, Object, Object> createDefaultResourceSet() {
        return super.createDefaultResourceSet().mapResponse(mapper);
    }

}
