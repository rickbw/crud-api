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


/**
 * Tests the nested subclass of {@link Settable} that handles
 * transforming responses.
 */
public class SettableAdaptNewValueTest extends SettableTest {

    private static final String RESPONSE_PREFIX = "Goodbye, cruel ";

    private static final Func1<Observable<Object>, Observable<Object>> adapter = new Func1<Observable<Object>, Observable<Object>>() {
        @Override
        public Observable<Object> call(final Observable<Object> input) {
            return input.map(new Func1<Object, Object>() {
                @Override
                public Object call(final Object obj) {
                    return RESPONSE_PREFIX + obj;
                }
            });
        }
    };


    @Override
    @Test
    public void resourceCallsDelegate() {
        // given:
        final Settable<Object, Object> resource = createDefaultResource();
        final Observable<Object> original = createDefaultResourceState();
        final Observable<Object> adapted = adapter.call(original);

        // when:
        resource.set(original);

        // then:
        @SuppressWarnings("rawtypes")
        final ArgumentCaptor<Observable> captor = ArgumentCaptor.forClass(Observable.class);
        verify(this.mockDelegate).set(captor.capture());
        final Observable<String> actual = captor.getValue();
        assertObservablesEqual(adapted, actual);
    }

    @Override
    protected Settable<Object, Object> createDefaultResource() {
        return super.createDefaultResource().<Object>adaptNewValue(adapter);
    }

}
