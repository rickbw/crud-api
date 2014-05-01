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
package rickbw.crud;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Assert;

import com.google.common.collect.Lists;

import rx.Observable;


/**
 * A set of test assertions, similar to {@link Assert}, that operate on RxJava
 * types.
 */
public final class RxAssertions {

    public static void assertObservablesEqual(
            final Observable<?> expected,
            final Observable<?> actual) {
        // Observables should never be null:
        assertNotNull(expected);
        assertNotNull(actual);

        // materialize() to collapse exceptions and values together
        final List<?> expectedValues = Lists.newArrayList(expected
                .materialize()
                .toBlockingObservable()
                .toIterable());
        final List<?> actualValues = Lists.newArrayList(actual
                .materialize()
                .toBlockingObservable()
                .toIterable());
        assertEquals(expectedValues, actualValues);
    }

    private RxAssertions() {
        // prevent instantiation
    }

}