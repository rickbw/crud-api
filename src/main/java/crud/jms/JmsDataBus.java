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

import crud.core.DataBus;
import crud.core.MiddlewareException;
import crud.core.ReadableResourceSet;
import crud.core.ResourceSet;
import crud.core.Session;
import crud.core.Session.Ordering;
import crud.core.TransactedSession;
import crud.core.WritableResourceSet;
import crud.implementer.DataBusWorker;
import rx.Observable;
import rx.functions.Func1;


public class JmsDataBus implements DataBus {

    private static final Logger log = LoggerFactory.getLogger(JmsDataBus.class);

    private static final Set<Ordering> supportedOrderings = Collections.unmodifiableSet(
            EnumSet.allOf(Session.Ordering.class));

    /**
     * TODO: Add support for application-level acknowledgments.
     */
    private static final int ORDERED_ACKNOWLEDGE_MODE = javax.jms.Session.AUTO_ACKNOWLEDGE;
    private static final int UNORDERED_ACKNOWLEDGE_MODE = javax.jms.Session.DUPS_OK_ACKNOWLEDGE;

    /**
     * Makes sure that our {@link Session}s are all shut down when this
     * {@link DataBus} is shut down. Normally that's automatic with JMS, but
     * this wrapper layer has some additional state that it manages.
     */
    private @Nonnull final DataBusWorker worker = DataBusWorker.create();
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
     * All JMS {@link ReadableResourceSet}s have key type String -- a message selector --
     * and element type Message. Passing any other types will result in a
     * result of {@link Optional#asSet()}.
     *
     * The {@link crud.core.ReadableResourceSet.Id#getName() name} of the
     * {@link crud.core.ReadableResourceSet.Id} is taken to be the name of a JMS
     * {@link Destination}.
     */
    @Override
    public <K, E> Optional<ReadableResourceSet<K, E>> resources(final ReadableResourceSet.Id<K, E> id) {
        if (!keyAndElementTypesAcceptable(id)) {
            return Optional.absent();
        }

        @Nullable final Destination destination = this.destinationLookup.call(id.getName());
        if (destination == null) {
            return Optional.absent();
        }

        return createReadableResourceSet(id, destination);
    }

    @Override
    public <K, E, R> Optional<WritableResourceSet<K, E, R>> resources(final WritableResourceSet.Id<K, E, R> id) {
        if (!keyAndElementTypesAcceptable(id)) {
            return Optional.absent();
        }
        if (Void.class != id.getWriteResultType()) {
            log.warn("JMS ResourceSets have write-result type Void, not {}", id.getWriteResultType().getName());
            return Optional.absent();
        }

        @Nullable final Destination destination = this.destinationLookup.call(id.getName());
        if (destination == null) {
            return Optional.absent();
        }

        return createWritableResourceSet(id, destination);
    }

    @Override
    public Set<Session.Ordering> getSupportedSessionOrderings() {
        return supportedOrderings;
    }

    @SuppressWarnings("resource")
    @Override
    public Session startSession(final boolean requireOrdering) {
        try {
            final javax.jms.Session delegateSession = this.connection.createSession(
                    false,
                    requireOrdering ? ORDERED_ACKNOWLEDGE_MODE : UNORDERED_ACKNOWLEDGE_MODE);
            return new NonTransactedJmsSession(this.worker, delegateSession);
        } catch (final JMSException jx) {
            throw new MiddlewareException(jx.getMessage(), jx);
        }
    }

    @SuppressWarnings("resource")
    @Override
    public TransactedSession startTransactedSession() {
        try {
            final javax.jms.Session delegateSession = this.connection.createSession(
                    true,
                    ORDERED_ACKNOWLEDGE_MODE);
            return new TransactedJmsSession(this.worker, delegateSession);
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

    private static <K, E> Optional<ReadableResourceSet<K, E>> createReadableResourceSet(
            final ReadableResourceSet.Id<K, E> id,
            final Destination destination) {
        /* All of these unchecked conversions are necessary, because the
         * method signature requires dynamic typing, but in this case, the
         * types are actually static.
         */
        @SuppressWarnings("unchecked")
        final ReadableResourceSet.Id<String, ? extends Message> msgResourceSetId = (ReadableResourceSet.Id<String, ? extends Message>) id;
        final ReadableResourceSet<String, ? extends Message> jmsResourceSet = new MessageConsumingResourceSet<>(msgResourceSetId, destination);
        @SuppressWarnings("rawtypes")
        final Optional untypedResourceSet = Optional.of(jmsResourceSet);
        @SuppressWarnings("unchecked")
        final Optional<ReadableResourceSet<K, E>> typedResourceSet = untypedResourceSet;
        return typedResourceSet;
    }

    private static <K, E, R> Optional<WritableResourceSet<K, E, R>> createWritableResourceSet(
            final WritableResourceSet.Id<K, E, R> id,
            final Destination destination) {
        /* All of these unchecked conversions are necessary, because the
         * method signature requires dynamic typing, but in this case, the
         * types are actually static.
         */
        @SuppressWarnings("unchecked")
        final WritableResourceSet.Id<String, ? extends Message, Void> msgResourceSetId = (WritableResourceSet.Id<String, ? extends Message, Void>) id;
        final WritableResourceSet<String, ? extends Message, Void> jmsResourceSet = new MessageProducingResourceSet<>(msgResourceSetId, destination);
        @SuppressWarnings("rawtypes")
        final Optional untypedResourceSet = Optional.of(jmsResourceSet);
        @SuppressWarnings("unchecked")
        final Optional<WritableResourceSet<K, E, R>> typedResourceSet = untypedResourceSet;
        return typedResourceSet;
    }

    private static boolean keyAndElementTypesAcceptable(final ResourceSet.Id<?, ?> id) {
        if (String.class != id.getKeyType()) {
            log.warn("JMS ResourceSets have key type String, not {}", id.getKeyType().getName());
            return false;
        }
        if (!Message.class.isAssignableFrom(id.getResourceType())) {
            log.warn("JMS ResourceSets have element type Message, not {}", id.getResourceType().getName());
            return false;
        }
        return true;
    }

}
