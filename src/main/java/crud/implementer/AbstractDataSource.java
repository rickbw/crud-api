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

import crud.core.DataSource;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;


public abstract class AbstractDataSource<E> extends AbstractSessionParticipant implements DataSource<E> {

    @Override
    public Observable<E> read() {
        return Observable.create(new Observable.OnSubscribe<E>() {
            @Override
            public void call(final Subscriber<? super E> sub) {
                submit(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        onReadSubscribe(sub);
                        return null;
                    }
                });
            }
        });
    }

    protected AbstractDataSource(@Nonnull final SessionWorker worker) {
        super(worker);
    }

    /**
     * Perform the implementation of the {@link #read()} operation here. This
     * method will be called from a task executed by the {@link SessionWorker}.
     * It is not necessary to call {@link Observer#onCompleted()} at the end;
     * it will be called automatically.
     *
     * This base implementation throws {@link AssertionError}. If a subclass
     * wishes to use the template implementation in {@link #read()}, it must
     * override this method. However, if it overrides {@link #read()}
     * directly, then there is no need to override this method.
     *
     * @param sub   The {@link Subscriber} to notify of the elements being read.
     */
    protected void onReadSubscribe(final Subscriber<? super E> sub) {
        throw new AssertionError("Must override this method if not overriding read()");
    }

}
