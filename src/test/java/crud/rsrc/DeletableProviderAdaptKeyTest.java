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

import static org.mockito.Mockito.verify;
import rx.functions.Func1;


public class DeletableProviderAdaptKeyTest
extends DeletableProviderTest {

    private static final String PREFIX = "Goodbye, cruel ";

    private final Func1<Object, String> adapter = new Func1<Object, String>() {
        @Override
        public String call(final Object response) {
            return PREFIX + response;
        }
    };


    @Override
    public void providerCallsDelegate() {
        // given:
        final DeletableProvider<Object, Object> provider = createDefaultProvider();
        final Object origKey = createDefaultKey();
        final String transformedKey = adapter.call(origKey);

        // when:
        provider.deleter(origKey);

        // then:
        verify(this.mockProvider).deleter(transformedKey);
    }

    @Override
    public void functionCallsDelegate() {
        // given:
        final DeletableProvider<Object, Object> provider = createDefaultProvider();
        final Func1<Object, Deletable<Object>> function = provider.toFunction();
        final Object origKey = createDefaultKey();
        final String transformedKey = adapter.call(origKey);

        // when:
        function.call(origKey);

        // then:
        verify(this.mockProvider).deleter(transformedKey);
    }

    @Override
    protected DeletableProvider<Object, Object> createDefaultProvider() {
        return super.createDefaultProvider().<Object>adaptKey(adapter);
    }

}
