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
import javax.jms.CompletionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;

import crud.core.DataSink;
import crud.core.MiddlewareException;
import rx.Observable;
import rx.Subscriber;


/*package*/ class MessageProducerDataSink<M extends Message> implements DataSink<M, Void> {

    private @Nonnull final MessageProducer producer;


    public MessageProducerDataSink(@Nonnull final MessageProducer producer) {
        this.producer = Objects.requireNonNull(producer);
    }

    @Override
    public Observable<Void> write(final M message) {
        final Observable<Void> hotResult = Observable.create(new CompletionListenerToSubscriberHandoff(message)).cache();
        /* Start a subscription now, so that the send() occurs immediately.
         * The no-argument subscribe() does not handle errors, so
         * materialize() to prevent it from seeing any.
         */
        hotResult.materialize().subscribe();
        return hotResult;
    }

    @Override
    public Observable<Void> shutdown() {
        try {
            /* TODO: JMS allows close() to be called from any thread.
             * However, it may block, to better to move it elsewhere.
             */
            this.producer.close();
            return Observable.empty();
        } catch (final JMSException jx) {
            return Observable.error(new MiddlewareException(jx.getMessage(), jx));
        }
    }


    private final class CompletionListenerToSubscriberHandoff implements Observable.OnSubscribe<Void> {
        private @Nonnull final M sendMe;

        public CompletionListenerToSubscriberHandoff(@Nonnull final M sendMe) {
            this.sendMe = Objects.requireNonNull(sendMe);
        }

        @Override
        public void call(final Subscriber<? super Void> sub) {
            try {
                MessageProducerDataSink.this.producer.send(this.sendMe, new CompletionListener() {
                    @Override
                    public void onCompletion(final Message ignored) {
                        sub.onCompleted();
                    }

                    @Override
                    public void onException(final Message ignored, final Exception exception) {
                        sub.onError(exception);
                    }
                });
            } catch (final JMSException jx) {
                // Becomes Observer.onError():
                throw new MiddlewareException(jx.getMessage(), jx);
            }
        }
    }

}
