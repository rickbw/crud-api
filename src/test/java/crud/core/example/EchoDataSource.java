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
package crud.core.example;

import java.util.Objects;

import javax.annotation.Nonnull;

import crud.implementer.AbstractDataSource;
import crud.implementer.SessionWorker;
import rx.Subscriber;


/**
 * Submits the data element provided in its constructor on every {@link #read()}.
 *
 * @author Rick Warren
 */
/*package*/ final class EchoDataSource<E> extends AbstractDataSource<E> {

    private @Nonnull final E message;


    public EchoDataSource(@Nonnull final SessionWorker worker, @Nonnull final E message) {
        super(worker);
        this.message = Objects.requireNonNull(message);
    }

    @Override
    protected void onReadSubscribe(final Subscriber<? super E> sub) {
        sub.onNext(this.message);
    }

}
