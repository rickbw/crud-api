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
package crud;

import com.google.common.base.Optional;


/**
 * A container for {@link ResourceProvider}s of different generic types.
 */
public interface ResourceProviderRegistry<KEY> {

    <K extends KEY, RSRC> Optional<ReadableResourceProvider<KEY, RSRC>> getReadableResourceProvider(
            Class<K> keyClass,
            Class<RSRC> rsrcClass);

}
