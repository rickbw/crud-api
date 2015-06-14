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

import static org.mockito.Mockito.verify;

import crud.core.Session;
import rx.functions.Func1;
import rx.functions.Func2;


public class TransformedWritableResourceSetAdaptKeyTest
extends TransformedWritableResourceSetTest {

    private static final String PREFIX = "Goodbye, cruel ";

    private final Func1<Object, String> adapter = new Func1<Object, String>() {
        @Override
        public String call(final Object response) {
            return PREFIX + response;
        }
    };


    @Override
    public void transformedResourceSetCallsDelegate() {
        // given:
        final TransformedWritableResourceSet<Object, Object, Object> rsrcSet = createDefaultResourceSet();
        final Object origKey = createDefaultKey();
        final String transformedKey = this.adapter.call(origKey);

        // when:
        rsrcSet.get(origKey, this.mockSession);

        // then:
        verify(this.mockResourceSet).get(transformedKey, this.mockSession);
    }

    @Override
    public void functionCallsDelegate() {
        // given:
        final TransformedWritableResourceSet<Object, Object, Object> rsrcSet = createDefaultResourceSet();
        final Func2<Object, Session, TransformedWritableResource<Object, Object>> function = rsrcSet.toFunction();
        final Object origKey = createDefaultKey();
        final String transformedKey = this.adapter.call(origKey);

        // when:
        function.call(origKey, this.mockSession);

        // then:
        verify(this.mockResourceSet).get(transformedKey, this.mockSession);
    }

    @Override
    protected TransformedWritableResourceSet<Object, Object, Object> createDefaultResourceSet() {
        return super.createDefaultResourceSet().<Object>adaptKey(this.adapter);
    }

}
