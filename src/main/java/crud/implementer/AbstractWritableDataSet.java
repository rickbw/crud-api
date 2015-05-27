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

import crud.core.WritableDataSet;


/**
 * An implementation of {@link WritableDataSet} that stores the
 * {@link crud.core.WritableDataSet.Id} on behalf of its subclasses.
 *
 * @author Rick Warren
 */
public abstract class AbstractWritableDataSet<K, E, R>
extends AbstractDataSet<K, E>
implements WritableDataSet<K, E, R> {

    @Override
    @SuppressWarnings("unchecked")
    public WritableDataSet.Id<K, E, R> getId() {
        return (WritableDataSet.Id<K, E, R>) super.getId();
    }

    protected AbstractWritableDataSet(@Nonnull final WritableDataSet.Id<K, E, R> id) {
        super(id);
    }

}
