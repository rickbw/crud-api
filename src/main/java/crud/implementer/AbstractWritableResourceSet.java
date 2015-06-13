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

import crud.core.WritableResourceSet;


/**
 * An implementation of {@link WritableResourceSet} that stores the
 * {@link crud.core.WritableResourceSet.Id} on behalf of its subclasses.
 *
 * @author Rick Warren
 */
public abstract class AbstractWritableResourceSet<KEY, RSRC, RESPONSE>
extends AbstractResourceSet<KEY, RSRC>
implements WritableResourceSet<KEY, RSRC, RESPONSE> {

    @Override
    @SuppressWarnings("unchecked")
    public WritableResourceSet.Id<KEY, RSRC, RESPONSE> getId() {
        return (WritableResourceSet.Id<KEY, RSRC, RESPONSE>) super.getId();
    }

    protected AbstractWritableResourceSet(@Nonnull final WritableResourceSet.Id<KEY, RSRC, RESPONSE> id) {
        super(id);
    }

}
