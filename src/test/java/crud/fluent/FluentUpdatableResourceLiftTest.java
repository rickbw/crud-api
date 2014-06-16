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
package crud.fluent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;


/**
 * Tests the nested subclass of {@link FluentUpdatableResource} that handles
 * lifting subscriptions.
 */
public class FluentUpdatableResourceLiftTest extends FluentUpdatableResourceTest {

    private final AtomicBoolean lifterCalled = new AtomicBoolean(false);
    private final Observable.Operator<Object, Object> lifter = new Observable.Operator<Object, Object>() {
        @Override
        public Subscriber<? super Object> call(final Subscriber<? super Object> subscriber) {
            lifterCalled.set(true);
            return subscriber;
        }
    };


    @Test
    public void lifterCalled() {
        // given:
        final FluentUpdatableResource<Object, Object> resource = createDefaultResource();
        final Object update = createDefaultUpdate();
        final String expectedResponseValue = "Response!";

        // when:
        when(super.mockDelegate.update(update)).thenReturn(Observable.<Object>from(expectedResponseValue));
        final Observable<Object> response = resource.update(update);

        // then:
        final Object actualResponseValue = response.toBlocking().first();
        // Test lifter doesn't actually do anything:
        assertEquals(expectedResponseValue, actualResponseValue);
        assertTrue(this.lifterCalled.get());    // after getting value
    }

    @Override
    protected FluentUpdatableResource<Object, Object> createDefaultResource() {
        return super.createDefaultResource().lift(this.lifter);
    }

}
