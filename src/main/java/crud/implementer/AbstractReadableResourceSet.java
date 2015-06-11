/* Copyright 2015 Rick Warren
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
package crud.implementer;

import javax.annotation.Nonnull;

import crud.core.ReadableResourceSet;


/**
 * An implementation of {@link ReadableResourceSet} that stores the
 * {@link crud.core.ReadableResourceSet.Id} on behalf of its subclasses.
 *
 * @author Rick Warren
 */
public abstract class AbstractReadableResourceSet<K, E>
extends AbstractResourceSet<K, E>
implements ReadableResourceSet<K, E> {

    @Override
    public ReadableResourceSet.Id<K, E> getId() {
        return (ReadableResourceSet.Id<K, E>) super.getId();
    }

    protected AbstractReadableResourceSet(@Nonnull final ReadableResourceSet.Id<K, E> id) {
        super(id);
    }

}
