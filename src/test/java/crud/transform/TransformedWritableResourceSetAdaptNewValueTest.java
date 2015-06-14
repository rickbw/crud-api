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

import org.junit.Test;

import rx.functions.Func1;


public class TransformedWritableResourceSetAdaptNewValueTest
extends TransformedWritableResourceSetTest {

    private static final String PREFIX = "Goodbye, cruel ";

    private final Func1<Object, String> adapter = new Func1<Object, String>() {
        @Override
        public String call(final Object response) {
            return PREFIX + response;
        }
    };


    @Test
    public void passAdaptedValueToResource() {
        // given:
        final TransformedWritableResourceSet<Object, Object, Object> rsrcSet = createDefaultResourceSet();
        final Object key = createDefaultKey();
        final String origValue = "World!";
        final String adaptedValue = this.adapter.call(origValue);

        // when:
        final TransformedWritableResource<Object, Object> resource = rsrcSet.get(key, this.mockSession);
        resource.write(origValue);

        // then:
        verify(super.mockResource).write(adaptedValue);
    }

    @Override
    protected TransformedWritableResourceSet<Object, Object, Object> createDefaultResourceSet() {
        return super.createDefaultResourceSet().<Object>adaptNewValue(this.adapter);
    }

}
