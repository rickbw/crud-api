/* Copyright 2013–2014 Rick Warren
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

package rickbw.crud.util;


/**
 * Patterned after Guava's class of the same name.
 */
public final class Preconditions {

    public static <T> T checkNotNull(final T value) {
        if (null == value) {
            throw new NullPointerException();
        }
        return value;
    }

    public static <T> T checkNotNull(final T value, final String message) {
        if (null == value) {
            throw new NullPointerException(message);
        }
        return value;
    }

    public static void checkArgument(final boolean value, final String message) {
        if (!value) {
            throw new IllegalArgumentException(message);
        }
    }

    private Preconditions() {
        // prevent instantiation
    }

}
