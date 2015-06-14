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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;

import rx.Observable;
import rx.functions.Func1;


public class FluentReadableResourceSetMapValueTest
extends FluentReadableResourceSetTest {

    private static final String PREFIX = "Goodbye, cruel ";

    private final Func1<Observable<?>, Observable<Object>> mapper = new Func1<Observable<?>, Observable<Object>>() {
        @Override
        public Observable<Object> call(final Observable<?> allInput) {
            return allInput.map(new Func1<Object, Object>() {
                @Override
                public Object call(final Object oneInput) {
                    return PREFIX + oneInput;
                }
            });
        }
    };


    @Test
    public void transformationApplied() {
        // given:
        final FluentReadableResourceSet<Object, Object> rsrcSet = createDefaultResourceSet();
        final Object key = createDefaultKey();

        // when:
        when(super.mockResource.read()).thenReturn(Observable.<Object>just("world"));
        final FluentReadableResource<Object> resource = rsrcSet.get(key);
        final Observable<Object> response = resource.read();

        // then:
        final String responseString = (String) response.toBlocking().single();
        assertTrue(responseString.startsWith(PREFIX));
    }

    @Override
    protected FluentReadableResourceSet<Object, Object> createDefaultResourceSet() {
        return super.createDefaultResourceSet().mapValue(this.mapper);
    }

}
