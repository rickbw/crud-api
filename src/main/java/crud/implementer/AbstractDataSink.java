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
package crud.implementer;

import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import crud.core.DataSink;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;


public abstract class AbstractDataSink<E, R>
extends AbstractSessionParticipant
implements DataSink<E, R> {

    /**
     * Write the given value in the background thread belonging to the
     * {@link SessionWorker}. This template implementation
     * {@link Observable#cache() caches} the result of the
     * {@link #doWrite(Object, Subscriber) write}, and thus assumes that those
     * results are few in number.
     *
     * @see #doWrite(Object, Subscriber)
     */
    @Override
    public Observable<R> write(final E value) {
        final Observable<R> result = Observable.create(new SubmitWriteOnSubscribe(value)).cache();
        /* Start a subscription now, so that the write is scheduled
         * immediately. The no-argument subscribe() does not handle errors,
         * so materialize() to prevent it from seeing any.
         */
        result.materialize().subscribe();
        return result;
    }

    protected AbstractDataSink(@Nonnull final SessionWorker worker) {
        super(worker);
    }

    /**
     * Perform the implementation of the {@link #write(Object)} operation
     * here. This method will be called from a task executed by the
     * {@link SessionWorker}. It is not necessary to call
     * {@link Observer#onCompleted()} at the end; it will be called
     * automatically.
     *
     * This base implementation throws {@link AssertionError}. If a subclass
     * wishes to use the template implementation in {@link #write(Object)}, it
     * must override this method. However, if it overrides
     * {@link #write(Object)} directly, then there is no need to override this
     * method.
     *
     * @param writeMe   The value to write.
     * @param resultSub The subscriber to which the result of the write should
     *                  be reported.
     */
    protected void doWrite(final E writeMe, final Subscriber<? super R> resultSub) {
        throw new AssertionError("Must override this method if not overriding write()");
    }


    private final class SubmitWriteOnSubscribe implements Observable.OnSubscribe<R> {
        private final E writeMe;

        public SubmitWriteOnSubscribe(final E writeMe) {
            this.writeMe = writeMe;
        }

        @Override
        public void call(final Subscriber<? super R> sub) {
            submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    doWrite(SubmitWriteOnSubscribe.this.writeMe, sub);
                    return null;
                }
            });
        }
    }

}
