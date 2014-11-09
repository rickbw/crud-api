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


public class UpdatableProviderAdaptUpdateTest
extends UpdatableProviderTest {

    private static final String PREFIX = "Goodbye, cruel ";

    private static final Func1<Observable<Object>, Observable<Object>> adapter = new Func1<Observable<Object>, Observable<Object>>() {
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
    public void passAdaptedValueToResource() {
        // given:
        final UpdatableProvider<Object, Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();
        final Observable<Object> origUpdate = Observable.<Object>just("World!");
        final Observable<Object> adaptedUpdate = adapter.call(origUpdate);

        // when:
        final Updatable<Object, Object> resource = provider.updater(key);
        resource.update(origUpdate);

        // then:
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Observable> captor = ArgumentCaptor.forClass(Observable.class);
        verify(this.mockResource).update(captor.capture());
        final Observable<String> actual = captor.getValue();
        assertObservablesEqual(adaptedUpdate, actual);
    }

    @Override
    protected UpdatableProvider<Object, Object, Object> createDefaultProvider() {
        return super.createDefaultProvider().<Object>adaptUpdate(adapter);
    }

}
