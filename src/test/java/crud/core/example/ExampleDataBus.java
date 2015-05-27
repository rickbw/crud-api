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
package crud.core.example;

import java.io.Writer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import crud.core.ReadableDataSet;
import crud.core.Session;
import crud.core.WritableDataSet;
import crud.implementer.AbstractDataBus;


public class ExampleDataBus extends AbstractDataBus {

    @Override
    public Set<Session.Ordering> getSupportedSessionOrderings() {
        return Collections.unmodifiableSet(EnumSet.of(
                Session.Ordering.UNORDERED,
                Session.Ordering.ORDERED));
    }

    @Override
    protected Session doStartOrderedSession() {
        return new ExampleSession();
    }

    @Override
    protected boolean isDataSetAvailable(final ReadableDataSet.Id<?, ?> id) {
        return id.getKeyType() == id.getElementType();
    }

    @Override
    protected ReadableDataSet<?, ?> resolveDataSet(final ReadableDataSet.Id<?, ?> id) {
        // isDataSetAvailable() ensures type conversion is safe:
        @SuppressWarnings({ "rawtypes", "unchecked" })
        final EchoReadableDataSet result = new EchoReadableDataSet(id);
        return result;
    }

    @Override
    protected boolean isDataSetAvailable(final WritableDataSet.Id<?, ?, ?> id) {
        return Writer.class.isAssignableFrom(id.getKeyType())
            && String.class.isAssignableFrom(id.getElementType())
            && id.getWriteResultType().isAssignableFrom(Integer.class);
    }

    @Override
    protected WritableDataSet<?, ?, ?> resolveDataSet(final WritableDataSet.Id<?, ?, ?> id) {
        @SuppressWarnings("rawtypes")
        final WritableDataSet.Id rawId = id;
        @SuppressWarnings("unchecked")
        final WritableDataSet.Id<Writer, String, Integer> typedId = rawId;
        return new PrintingDataSet(typedId);
    }

}
