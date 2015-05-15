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
package crud.jdbc;

import java.util.Objects;

import crud.core.DataSet;
import crud.core.DataSetId;


/*package*/ class Table<K, E> implements DataSet<K, E> {

    private final DataSetId<K, E> id;


    public Table(final DataSetId<K, E> id) {
        this.id = Objects.requireNonNull(id);
    }

    @Override
    public DataSetId<K, E> getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + this.id + ')';
    }

}
