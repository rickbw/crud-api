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

import crud.core.ReadableDataSet;


/**
 * An implementation of {@link ReadableDataSet} that stores the
 * {@link crud.core.ReadableDataSet.Id} on behalf of its subclasses.
 *
 * @author Rick Warren
 */
public abstract class AbstractReadableDataSet<K, E>
extends AbstractDataSet<K, E>
implements ReadableDataSet<K, E> {

    @Override
    public ReadableDataSet.Id<K, E> getId() {
        return (ReadableDataSet.Id<K, E>) super.getId();
    }

    protected AbstractReadableDataSet(@Nonnull final ReadableDataSet.Id<K, E> id) {
        super(id);
    }

}
