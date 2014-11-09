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
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import rx.Observable;
import rx.functions.Func1;


public class SettableProviderAdaptNewValueTest
extends SettableProviderTest {

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
        final SettableProvider<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();
        final Observable<String> origValue = Observable.just("World!");
        final Observable<String> adaptedValue = origValue.map(adapter);

        // when:
        final Settable<Object, Object> resource = provider.setter(key);
        resource.set(origValue);

        // then:
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Observable> captor = ArgumentCaptor.forClass(Observable.class);
        verify(super.mockResource).set(captor.capture());
        assertObservablesEqual(adaptedValue, adaptedValue);
    }

    @Override
    protected SettableProvider<Object, Object, Object> createDefaultProvider() {
        return super.createDefaultProvider().<Object>adaptNewValue(adapter);
    }

}
