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

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import crud.core.DataBus;
import crud.core.MiddlewareException;
import crud.core.ReadableDataSet;
import crud.core.Session;
import crud.core.Session.Ordering;
import crud.core.WritableDataSet;
import rx.Observable;
import rx.functions.Func1;


public class JmsDataBus implements DataBus {

    private static final Logger log = LoggerFactory.getLogger(JmsDataBus.class);

    private static final Set<Ordering> supportedOrderings = Collections.unmodifiableSet(
            EnumSet.allOf(Session.Ordering.class));

    /**
     * TODO: All this mapping to be injected, or otherwise configured.
     *
     * TODO: Add support for application-level acknowledgments.
     */
    private static final ImmutableMap<Session.Ordering, Integer> acknowledgeModes = ImmutableMap.of(
            Session.Ordering.UNORDERED, javax.jms.Session.DUPS_OK_ACKNOWLEDGE,
            Session.Ordering.ORDERED, javax.jms.Session.AUTO_ACKNOWLEDGE,
            Session.Ordering.TRANSACTIONAL, javax.jms.Session.AUTO_ACKNOWLEDGE);

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
     * All JMS {@link ReadableDataSet}s have key type String -- a message selector --
     * and element type Message. Passing any other types will result in a
     * result of {@link Optional#asSet()}.
     *
     * The {@link crud.core.ReadableDataSet.Id#getName() name} of the
     * {@link crud.core.ReadableDataSet.Id} is taken to be the name of a JMS
     * {@link Destination}.
     */
    @Override
    public <K, E> Optional<ReadableDataSet<K, E>> dataSet(final ReadableDataSet.Id<K, E> id) {
        if (!Message.class.isAssignableFrom(id.getElementType())) {
            // JMS only support DataSets of type Message (or a subtype)
            log.warn("JMS DataSets have element type Message, not {}", id.getElementType().getName());
            return Optional.absent();
        }
        if (String.class != id.getKeyType()) {
            log.warn("JMS DataSets have key type String, not {}", id.getKeyType().getName());
            return Optional.absent();
        }

        @Nullable final Destination destination = this.destinationLookup.call(id.getName());
        if (destination == null) {
            return Optional.absent();
        }

        return createDataSet(id, destination);
    }

    @Override
    public <K, E, R> Optional<WritableDataSet<K, E, R>> dataSet(final WritableDataSet.Id<K, E, R> id) {
        return Optional.absent();   // TODO
    }

    @Override
    public Set<Session.Ordering> getSupportedSessionOrderings() {
        return supportedOrderings;
    }

    @SuppressWarnings("resource")
    @Override
    public Session startSession(final Session.Ordering requestedOrdering) {
        try {
            final boolean transacted = (requestedOrdering == Session.Ordering.TRANSACTIONAL);
            final int acknowledgeMode = acknowledgeModes.get(requestedOrdering);
            final javax.jms.Session delegateSession = this.connection.createSession(
                    transacted,
                    acknowledgeMode);
            return new SessionWrapper(delegateSession);
        } catch (final JMSException jx) {
            throw new MiddlewareException(jx.getMessage(), jx);
        }
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

    private static <K, E> Optional<ReadableDataSet<K, E>> createDataSet(
            final ReadableDataSet.Id<K, E> id,
            final Destination destination) {
        /* All of these unchecked conversions are necessary, because the
         * method signature requires dynamic typing, but in this case, the
         * types are actually static.
         */
        @SuppressWarnings("unchecked")
        final ReadableDataSet.Id<String, ? extends Message> msgDataSetId = (ReadableDataSet.Id<String, ? extends Message>) id;
        final ReadableDataSet<String, ? extends Message> jmsDataSet = new JmsDataSet<>(msgDataSetId, destination);
        @SuppressWarnings("rawtypes")
        final Optional untypedDataSet = Optional.of(jmsDataSet);
        @SuppressWarnings("unchecked")
        final Optional<ReadableDataSet<K, E>> typedDataSet = untypedDataSet;
        return typedDataSet;
    }

}
