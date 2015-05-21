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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import crud.core.DataSink;
import crud.core.MiddlewareException;
import crud.core.Session;
import crud.core.WritableDataSet;


/*package*/ class MessageProducingDataSet<M extends Message> implements WritableDataSet<String, M, Void> {

    private static final Logger log = LoggerFactory.getLogger(MessageProducingDataSet.class);

    private @Nonnull final WritableDataSet.Id<String, M, Void> id;
    private @Nonnull final Destination destination;


    public MessageProducingDataSet(
            @Nonnull final WritableDataSet.Id<String, M, Void> id,
            @Nonnull final Destination destination) {
        this.id = Objects.requireNonNull(id);
        this.destination = Objects.requireNonNull(destination);
    }

    @Override
    public WritableDataSet.Id<String, M, Void> getId() {
        return this.id;
    }

    @SuppressWarnings("resource")
    @Override
    public DataSink<M, Void> dataSink(final Session session, final String key) {
        if (!key.isEmpty()) {
            log.warn("Ignoring key {}", key);
        }

        final javax.jms.Session realSession = ((SessionWrapper) session).getDelegate();
        try {
            final MessageProducer messageProducer = realSession.createProducer(this.destination);
            return new MessageProducerDataSink<>(messageProducer);
        } catch (final JMSException jx) {
            throw new MiddlewareException(jx.getMessage(), jx);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '('
                + this.id
                + ", " + this.destination
                + ')';
    }

}
