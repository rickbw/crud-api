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

import org.junit.Test;

import rx.functions.Func1;


public class UpdatableProviderAdaptUpdateTest
extends UpdatableProviderTest {

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
        final UpdatableProvider<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();
        final String origUpdate = "World!";
        final String adaptedUpdate = adapter.call(origUpdate);

        // when:
        final Updatable<Object, Object> resource = provider.updater(key);
        resource.update(origUpdate);

        // then:
        verify(super.mockResource).update(adaptedUpdate);
    }

    @Override
    protected UpdatableProvider<Object, Object, Object> createDefaultProvider() {
        return super.createDefaultProvider().<Object>adaptUpdate(adapter);
    }

}