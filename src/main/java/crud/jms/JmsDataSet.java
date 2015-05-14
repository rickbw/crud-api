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
package crud.jms;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.jms.Destination;
import javax.jms.Message;

import crud.core.DataSet;
import crud.core.DataSetId;


/*package*/ class JmsDataSet implements DataSet<String, Message> {

    private @Nonnull final DataSetId<String, Message> id;
    private @Nonnull final Destination destination;


    public JmsDataSet(
            @Nonnull final DataSetId<String, Message> id,
            @Nonnull final Destination destination) {
        this.id = Objects.requireNonNull(id);
        this.destination = Objects.requireNonNull(destination);
    }

    @Override
    public DataSetId<String, Message> getId() {
        return this.id;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '('
                + this.id
                + ", " + this.destination
                + ')';
    }

    /*package*/ @Nonnull Destination getDestination() {
        return this.destination;
    }

}
