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
import javax.annotation.Nullable;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import crud.core.DataBus;
import crud.core.DataSet;
import crud.core.DataSetId;
import crud.core.MiddlewareException;
import rx.Observable;
import rx.functions.Func1;


public class JmsDataBus implements DataBus {

    private static final Logger log = LoggerFactory.getLogger(JmsDataBus.class);

    private @Nonnull final Connection connection;
    private @Nonnull final Func1<String, Destination> destinationLookup;


    public JmsDataBus(
            @Nonnull final Connection connection,
            @Nonnull final Func1<String, Destination> destinationLookup) {
        this.connection = Objects.requireNonNull(connection);
        this.destinationLookup = Objects.requireNonNull(destinationLookup);
    }

    @Override
    public void start() {
        try {
            this.connection.start();
        } catch (final JMSException jx) {
            throw new MiddlewareException(jx.getMessage(), jx);
        }
    }

    /**
     * All JMS {@link DataSet}s have key type String -- a message selector --
     * and element type Message. Passing any other types will result in a
     * result of {@link Optional#asSet()}.
     *
     * The {@link DataSetId#getName() name} of the {@link DataSetId} is taken
     * to be the name of a JMS {@link Destination}.
     */
    @Override
    public <K, E> Optional<DataSet<K, E>> dataSet(final DataSetId<K, E> id) {
        if (!Message.class.isAssignableFrom(id.getElementType())) {
            // JMS only support DataSets of type Message (or a subtype)
            log.warn("JMS DataSets have element type Message, not {}", id.getElementType().getName());
            return Optional.absent();
        }
        if (!String.class.isAssignableFrom(id.getKeyType())) {
            log.warn("JMS DataSets have key type String, not {}", id.getKeyType().getName());
            return Optional.absent();
        }

        @Nullable final Destination destination = this.destinationLookup.call(id.getName());
        if (destination == null) {
            return Optional.absent();
        }

        /* Has to be unchecked, because the method signature requires dynamic
         * typing, but in this case, the types are actually static.
         */
        @SuppressWarnings("unchecked")
        final Optional<DataSet<K, E>> dataSet = createDataSet(id, destination);
        return dataSet;
    }

    @Override
    public Observable<Void> shutdown() {
        try {
            this.connection.close();
            return Observable.empty();
        } catch (final JMSException jx) {
            return Observable.error(new MiddlewareException(jx.getMessage(), jx));
        }
    }

    /**
     * Generics have to be unchecked, because the signature of
     * {@link #dataSet(DataSetId)} requires dynamic typing, but in this case,
     * the types are actually static.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Optional createDataSet(final DataSetId id, final Destination destination) {
        final JmsDataSet dataSet = new JmsDataSet(id, destination);
        return Optional.of(dataSet);
    }

}
