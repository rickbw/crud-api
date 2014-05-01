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


public class FluentUpdatableResourceProviderMapResponseTest
extends FluentUpdatableResourceProviderTest {

    private static final String PREFIX = "Goodbye, cruel ";

    private final Func1<Object, String> mapper = new Func1<Object, String>() {
        @Override
        public String call(final Object response) {
            return PREFIX + response;
        }
    };


    @Test
    public void transformationApplied() {
        // given:
        final FluentUpdatableResourceProvider<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();
        final String update = "Hello";
        final String origResponse = "world";
        final String mappedResponse = mapper.call(origResponse);

        // when:
        when(super.mockResource.update(update)).thenReturn(Observable.<Object>from(origResponse));
        final FluentUpdatableResource<Object, Object> resource = provider.get(key);
        final Observable<Object> response = resource.update(update);

        // then:
        final String responseString = (String) response.toBlockingObservable().single();
        assertEquals(mappedResponse, responseString);
    }

    @Override
    protected FluentUpdatableResourceProvider<Object, Object, Object> createDefaultProvider() {
        return super.createDefaultProvider().<Object>mapResponse(mapper);
    }

}
