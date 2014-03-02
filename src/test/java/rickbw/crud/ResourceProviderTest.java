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

package rickbw.crud;

import org.junit.Test;


/**
 * Just make sure this thing keeps compiling.
 */
public final class ResourceProviderTest {

    public interface Readable extends ReadableResource<String> {
        // empty
    }

    public interface Writable extends WritableResource<Object, StringBuilder> {
        // empty
    }

    public interface ReadWritable extends Readable, Writable {
        // empty
    }

    public interface Provider extends ReadableResourceProvider<Enum<?>, String>, WritableResourceProvider<Enum<?>, Object, StringBuilder> {
        @Override
        public abstract ReadWritable get(Enum<?> key);
    }

    @Test
    public void testNothing() {
        // nothing to do
    }

}
