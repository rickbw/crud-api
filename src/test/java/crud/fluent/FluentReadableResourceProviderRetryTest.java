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

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.when;

import java.util.ConcurrentModificationException;

import org.junit.Test;

import rx.Observable;


public class FluentReadableResourceProviderRetryTest
extends FluentReadableResourceProviderTest {

    private static final int NUM_RETRIES = 2;
//    private static final String SUCCESS_RESPONSE = "Hello, World";


    @Test
    public void retryZeroTimesReturnsSameObject() {
        // given:
        final FluentReadableResourceProvider<Object, Object> expected = super.createDefaultProvider();

        // when:
        final FluentReadableResourceProvider<Object, Object> actual = expected.retry(0);

        // then:
        assertSame(expected, actual);
    }

    @Test(expected=IllegalArgumentException.class)
    public void retryNegativeTimesThrows() {
        // given:
        final FluentReadableResourceProvider<Object, Object> provider = super.createDefaultProvider();

        // when:
        provider.retry(-1);
    }

    /* TODO: Rewrite this test. It relies on a bug in a previous version of RxJava.
    @Test
    public void retryUntilSuccess() {
        // given:
        final FluentReadableResourceProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();
        final Observable<Object> firstAttemptAndAllRetries = Observable.from(ImmutableList.of(
                Notification.createOnError(new RuntimeException("1st attempt")),
                Notification.createOnError(new RuntimeException("1st retry")),
                Notification.createOnNext(SUCCESS_RESPONSE),
                Notification.createOnCompleted()))
            .dematerialize();

        // when:
        when(super.mockResource.get()).thenReturn(firstAttemptAndAllRetries);
        final FluentReadableResource<Object> resource = provider.get(key);
        final Observable<Object> response = resource.get();
        final Object value = response.toBlocking().single();

        // then:
        assertEquals(SUCCESS_RESPONSE, value);
    }
    */

    @Test(expected=ConcurrentModificationException.class)
    public void propagateExceptionWhenRetriesExceeded() {
        // given:
        final FluentReadableResourceProvider<Object, Object> provider = createDefaultProvider();
        final Object key = createDefaultKey();
        final Observable<Object> firstAttemptAndAllRetries = Observable.error(
                // Use unusual exception type to make sure we catch our own:
                new ConcurrentModificationException("throw over and over"));

        // when:
        when(super.mockResource.get()).thenReturn(firstAttemptAndAllRetries);
        final FluentReadableResource<Object> resource = provider.get(key);
        final Observable<Object> response = resource.get();
        response.toBlocking().single();
    }

    @Override
    protected FluentReadableResourceProvider<Object, Object> createDefaultProvider() {
        return super.createDefaultProvider().retry(NUM_RETRIES);
    }

}