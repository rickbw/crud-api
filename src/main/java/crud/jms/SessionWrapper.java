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
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;
import javax.jms.JMSException;

import crud.implementer.SessionWorker;

import crud.core.AsyncCloseable;
import rx.Observable;


/*package*/ abstract class SessionWrapper implements AsyncCloseable {

    private final SessionWorker worker = new SessionWorker();
    private @Nonnull final javax.jms.Session delegate;

    private final Callable<Void> closeTask = new Callable<Void>() {
        @Override
        public Void call() throws JMSException {
            getDelegate().close();
            return null;
        }
    };


    @Override
    public final Observable<Void> shutdown() {
        return this.worker.shutdown(this.closeTask, Long.MAX_VALUE, TimeUnit.NANOSECONDS);
    }

    protected SessionWrapper(@Nonnull final javax.jms.Session delegate) {
        this.delegate = Objects.requireNonNull(delegate);
    }

    protected final Observable<Void> submit(final Callable<Void> task) {
        return this.worker.submit(task);
    }

    protected final @Nonnull javax.jms.Session getDelegate() {
        return this.delegate;
    }

}
