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

import java.util.Objects;

import javax.annotation.Nonnull;

import crud.core.ResourceSet;


/**
 * An implementation of {@link ResourceSet} that stores the
 * {@link crud.core.ResourceSet.Id} on behalf of its subclasses.
 *
 * @author Rick Warren
 */
public abstract class AbstractResourceSet<KEY, RSRC> implements ResourceSet<KEY, RSRC> {

    private @Nonnull final ResourceSet.Id<KEY, RSRC> id;


    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + getId() + ')';
    }

    protected AbstractResourceSet(@Nonnull final ResourceSet.Id<KEY, RSRC> id) {
        this.id = Objects.requireNonNull(id);
    }

    protected ResourceSet.Id<KEY, RSRC> getId() {
        return this.id;
    }

}
