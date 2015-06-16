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

import rx.Observer;
import rx.Subscriber;


/**
 * An asynchronous task to be scheduled by a {@link SessionWorker} or
 * {@link DataBusWorker}. It will be wrapped by an instance of
 * {@link rx.Observable.OnSubscribe}, and calls to
 * {@link Observer#onCompleted()} or {@link Observer#onError(Throwable)} will
 * be taken care of on behalf of the task; it need only invoke
 * {@link Observer#onNext(Object)}.
 */
public interface Task<T> {

    void call(Subscriber<? super T> sub) throws Exception;

}
