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
package crud.spi;


/**
 * Look up a {@link WritableSpec} based on a given key.
 *
 * @see ReadableProviderSpec
 * @see DeletableProviderSpec
 * @see UpdatableProviderSpec
 * @see WritableSpec
 */
public interface WritableProviderSpec<KEY, RSRC, RESPONSE> extends ResourceProviderSpec {

    /**
     * Get the {@link UpdatableSpec} associated with the given key. Resources
     * returned for keys that are equal should themselves be equal. In no
     * case should a resource be {@code null}; the absence of state should be
     * handled by the {@code Resource} itself.
     *
     * This operation is assumed to be relatively cheap and in-memory.
     * Specifically, when dealing with remote and/or persistent Resources,
     * implementers should locate expensive operations -- such as transferring
     * data across a network -- within the Resource implementations
     * themselves, and leave the ResourceProviders to simply prepare the
     * necessary objects.
     *
     * @throws NullPointerException if the given key is null.
     */
    WritableSpec<RSRC, RESPONSE> writer(KEY key);

}
