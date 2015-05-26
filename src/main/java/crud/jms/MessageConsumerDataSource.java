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
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;

import crud.core.DataSource;
import crud.core.MiddlewareException;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;


/*package*/ final class MessageConsumerDataSource<M extends Message> implements DataSource<M> {

    private @Nonnull final SessionWrapper session;
    private @Nonnull final MessageConsumer consumer;
    private @Nonnull final Observable<M> hotObservable;


    public MessageConsumerDataSource(
            @Nonnull final SessionWrapper session,
            @Nonnull final MessageConsumer consumer,
            @Nonnull final Class<M> messageType) {
        this.session = Objects.requireNonNull(session);
        this.consumer = Objects.requireNonNull(consumer);

        this.hotObservable = Observable.create(new MessageListenerToSubscriberHandoff(messageType)).share();
        /* Start a subscription now, so that the MessageListener will be
         * immediately installed. The no-argument subscribe() does not handle
         * errors, so materialize() to prevent it from seeing any.
         */
        this.hotObservable.materialize().subscribe();
    }

    @Override
    public Observable<M> read() {
        return this.hotObservable;
    }

    @Override
    public Observable<Void> shutdown() {
        return this.session.submit(new Callable<Void>() {
            @Override
            public Void call() throws JMSException {
                /* TODO: Should this result in an onCompleted() to the
                 * hotObservable? Or perhaps an onError() with a specific
                 * source-termination "exception"?
                 */
                MessageConsumerDataSource.this.consumer.close();
                return null;
            }
        });
    }


    private final class MessageListenerToSubscriberHandoff implements Observable.OnSubscribe<M> {
        private final Class<M> messageType;

        public MessageListenerToSubscriberHandoff(final Class<M> messageType) {
            this.messageType = messageType;
        }

        @Override
        public void call(final Subscriber<? super M> sub) {
            try {
                sub.add(new MessageListenerRemover());
                MessageConsumerDataSource.this.consumer.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(final Message message) {
                        sub.onNext(MessageListenerToSubscriberHandoff.this.messageType.cast(message));
                    }
                });
            } catch (final JMSException jx) {
                // Will become a call to onError:
                throw new MiddlewareException(jx.getMessage(), jx);
            }
        }
    }


    private final class MessageListenerRemover implements Subscription {
        @Override
        public void unsubscribe() {
            try {
                MessageConsumerDataSource.this.consumer.setMessageListener(null);
            } catch (final JMSException jx) {
                throw new MiddlewareException(jx.getMessage(), jx);
            }
        }

        @Override
        public boolean isUnsubscribed() {
            try {
                return MessageConsumerDataSource.this.consumer.getMessageListener() == null;
            } catch (final JMSException jx) {
                throw new MiddlewareException(jx.getMessage(), jx);
            }
        }
    }

}
