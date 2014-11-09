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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;


/**
 * Tests the nested subclass of {@link Gettable} that handles
 * transforming responses.
 */
public class GettableProviderFlatMapValueTest
extends GettableProviderTest {

    private static final String RESPONSE_PREFIX = "Goodbye, cruel ";

    private static final Func1<Object, Observable<String>> mapper = new Func1<Object, Observable<String>>() {
        @Override
        public Observable<String> call(final Object input) {
            final String transformed = RESPONSE_PREFIX + input;
            return Observable.just(transformed);
        }
    };


    @Test
    public void transformationApplied() {
        // given:
        final GettableProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();
        final String origResponse = "Hello, World";
        final String mappedResponse = mapper.call(origResponse).toBlocking().first();

        // when:
        when(super.mockResource.get()).thenReturn(Observable.<Object>just(origResponse));
        final Gettable<Object> resource = provider.getter(key);
        final Observable<Object> response = resource.get();

        // then:
        final Object responseValue = response.toBlocking().first();
        assertEquals(mappedResponse, responseValue);
    }

    @Override
    protected GettableProvider<Object, Object> createDefaultProvider() {
        return super.createDefaultProvider().<Object>flatMapValue(mapper);
    }

}
