/* Copyright 2013–2014 Rick Warren
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

package rickbw.crud.util;

import rickbw.crud.DeletableResource;
import rickbw.crud.ReadableResource;
import rx.Observable;


public final class RetryDeletableResource<RESPONSE>
implements DeletableResource<RESPONSE> {

    private final ReadableResource<RESPONSE> readableDelegate;
    private final int maxRetries;


    public RetryDeletableResource(
            final DeletableResource<RESPONSE> delegate,
            final int maxRetries) {
        this.readableDelegate = FluentDeletableResource.from(delegate).asReadableResource();
        this.maxRetries = maxRetries;
        Preconditions.checkArgument(this.maxRetries >= 0, "Negative retries");
    }

    @Override
    public Observable<RESPONSE> delete() {
        final ReadableResource<RESPONSE> retryable = new RetryReadableResource<RESPONSE>(
                this.readableDelegate,
                this.maxRetries);
        final Observable<RESPONSE> response = retryable.get();
        return response;
    }

}
