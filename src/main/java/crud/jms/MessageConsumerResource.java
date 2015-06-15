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
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import crud.core.MiddlewareException;
import crud.core.ReadableResource;
import crud.implementer.SessionWorker;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;


/*package*/ final class MessageConsumerResource<M extends Message> implements ReadableResource<M> {

    private @Nonnull final SessionWorker worker;
    private @Nonnull final MessageConsumer consumer;
    private @Nonnull final Observable<M> sharedObservable;

    private final MessageListenerRemover messageListenerRemover = new MessageListenerRemover();


    public MessageConsumerResource(
            @Nonnull final SessionWorker worker,
            @Nonnull final MessageConsumer consumer,
            @Nonnull final Class<M> messageType) {
        this.worker = Objects.requireNonNull(worker);
        this.consumer = Objects.requireNonNull(consumer);

        this.worker.addPreShutdownHook(this);

        final MessageListenerToSubscriberHandoff task = new MessageListenerToSubscriberHandoff(messageType);
        this.sharedObservable = this.worker.scheduleCold(task).share();
    }

    @Override
    public Observable<M> read() {
        return this.sharedObservable;
    }

    @Override
    public Observable<Void> shutdown() {
        return this.worker.scheduleHot(new SessionWorker.Task<Void>() {
            @Override
            public void call(final Subscriber<? super Void> sub) throws JMSException {
                /* TODO: Should this result in an onCompleted() to the
                 * sharedObservable? Or perhaps an onError() with a specific
                 * source-termination "exception"?
                 */
                MessageConsumerResource.this.consumer.close();
            }
        });
    }


    private final class MessageListenerToSubscriberHandoff implements SessionWorker.Task<M> {
        private final Class<M> messageType;

        public MessageListenerToSubscriberHandoff(final Class<M> messageType) {
            this.messageType = messageType;
        }

        @Override
        public void call(final Subscriber<? super M> sub) throws JMSException {
            if (MessageConsumerResource.this.consumer.getMessageListener() == null) {
                sub.add(MessageConsumerResource.this.messageListenerRemover);
                MessageConsumerResource.this.consumer.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(final Message message) {
                        sub.onNext(MessageListenerToSubscriberHandoff.this.messageType.cast(message));
                    }
                });
            }
        }
    }


    private final class MessageListenerRemover implements Subscription {
        @Override
        public void unsubscribe() {
            try {
                MessageConsumerResource.this.consumer.setMessageListener(null);
            } catch (final JMSException jx) {
                throw new MiddlewareException(jx.getMessage(), jx);
            }
        }

        @Override
        public boolean isUnsubscribed() {
            try {
                return MessageConsumerResource.this.consumer.getMessageListener() == null;
            } catch (final JMSException jx) {
                throw new MiddlewareException(jx.getMessage(), jx);
            }
        }
    }

}
