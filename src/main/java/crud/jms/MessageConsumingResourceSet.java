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
import javax.jms.MessageConsumer;

import crud.core.MiddlewareException;
import crud.core.ReadableResource;
import crud.core.ReadableResourceSet;
import crud.core.Session;


/*package*/ class MessageConsumingResourceSet<M extends Message> implements ReadableResourceSet<String, M> {

    private @Nonnull final ReadableResourceSet.Id<String, M> id;
    private @Nonnull final Destination destination;


    public MessageConsumingResourceSet(
            @Nonnull final ReadableResourceSet.Id<String, M> id,
            @Nonnull final Destination destination) {
        this.id = Objects.requireNonNull(id);
        this.destination = Objects.requireNonNull(destination);
    }

    @Override
    @SuppressWarnings("resource")
    public ReadableResource<M> get(final String key, final Session session) {
        final SessionWrapper sessionImpl = (SessionWrapper) session;
        final javax.jms.Session realSession = sessionImpl.getDelegate();
        try {
            final MessageConsumer messageConsumer = key.isEmpty()
                    ? realSession.createConsumer(this.destination)
                    : realSession.createConsumer(this.destination, key);
            return new MessageConsumerResource<>(sessionImpl.getWorker(), messageConsumer, this.id.getResourceType());
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
