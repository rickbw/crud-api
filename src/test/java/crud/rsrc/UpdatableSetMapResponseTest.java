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


public class UpdatableSetMapResponseTest extends UpdatableSetTest {

    private static final String PREFIX = "Goodbye, cruel ";

    private static final Func1<Observable<Object>, Observable<Object>> mapper = new Func1<Observable<Object>, Observable<Object>>() {
        @Override
        public Observable<Object> call(final Observable<Object> input) {
            return input.map(new Func1<Object, Object>() {
                @Override
                public Object call(final Object obj) {
                    return PREFIX + obj;
                }
            });
        }
    };


    @Test
    public void transformationApplied() {
        // given:
        final UpdatableSet<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();
        final Observable<Object> update = Observable.<Object>just("Hello");
        final Observable<Object> origResponse = Observable.<Object>just("world");
        final Observable<Object> mappedResponse = mapper.call(origResponse);

        // when:
        when(super.mockResource.update(update)).thenReturn(origResponse);
        final Updatable<Object, Object> resource = provider.updater(key);
        final Observable<Object> response = resource.update(update);

        // then:
        assertObservablesEqual(mappedResponse, response);
    }

    @Override
    protected UpdatableSet<Object, Object, Object> createDefaultProvider() {
        return super.createDefaultProvider().<Object>mapResponse(mapper);
    }

}
