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
package crud.rsrc;

import static crud.RxAssertions.assertObservablesEqual;
import static org.mockito.Mockito.when;

import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;


/**
 * Tests the nested subclass of {@link Settable} that handles
 * transforming responses.
 */
public class SettableMapResponseTest extends SettableTest {

    private static final String RESPONSE_PREFIX = "Goodbye, cruel ";

    private static final Func1<Observable<Object>, Observable<Object>> mapper = new Func1<Observable<Object>, Observable<Object>>() {
        @Override
        public Observable<Object> call(final Observable<Object> inputObs) {
            return inputObs.map(new Func1<Object, Object>() {
                @Override
                public Object call(final Object input) {
                    return RESPONSE_PREFIX + input;
                }
            });
        }
    };


    @Test
    public void transformationApplied() {
        // given:
        final Settable<Object, Object> resource = createDefaultResource();
        final Observable<Object> newValue = createDefaultResourceState();
        final Observable<Object> origResponse = Observable.<Object>just("world");
        final Observable<Object> mappedResponse = mapper.call(origResponse);

        // when:
        when(super.mockDelegate.set(newValue)).thenReturn(origResponse);
        final Observable<Object> response = resource.set(newValue);

        // then:
        assertObservablesEqual(mappedResponse, response);
    }

    @Override
    protected Settable<Object, Object> createDefaultResource() {
        return super.createDefaultResource().<Object>mapResponse(mapper);
    }

}
