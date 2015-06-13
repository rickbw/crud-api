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

import crud.core.WritableResource;
import crud.implementer.SessionWorker;
import rx.Observable;
import rx.Subscriber;


/*package*/ final class MessageProducerResource<M extends Message> implements WritableResource<M, Void> {

    private @Nonnull final SessionWorker worker;
    private @Nonnull final MessageProducer producer;


    public MessageProducerResource(
            @Nonnull final SessionWorker worker,
            @Nonnull final MessageProducer producer) {
        this.worker = Objects.requireNonNull(worker);
        this.producer = Objects.requireNonNull(producer);
    }

    @Override
    public Observable<Void> write(final M message) {
        return this.worker.scheduleHot(new SessionWorker.Task<Void>() {
            @Override
            public void call(final Subscriber<? super Void> sub) throws JMSException {
                MessageProducerResource.this.producer.send(message, new CompletionListener() {
                    @Override
                    public void onCompletion(final Message ignored) {
                        sub.onCompleted();
                    }

                    @Override
                    public void onException(final Message ignored, final Exception exception) {
                        sub.onError(exception);
                    }
                });
            }
        });
    }

    @Override
    public Observable<Void> shutdown() {
        return this.worker.scheduleHot(new SessionWorker.Task<Void>() {
            @Override
            public void call(final Subscriber<? super Void> sub) throws JMSException {
                MessageProducerResource.this.producer.close();
            }
        });
    }

}
