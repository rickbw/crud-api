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

import rickbw.crud.ReadableResource;
import rickbw.crud.WritableResource;
import rx.Observable;


public final class RetryWritableResource<RSRC, RESPONSE>
implements WritableResource<RSRC, RESPONSE> {

    private final WritableResource<? super RSRC, RESPONSE> delegateSetter;
    private final int maxRetries;


    public RetryWritableResource(
            final WritableResource<? super RSRC, RESPONSE> delegate,
            final int maxRetries) {
        this.delegateSetter = Preconditions.checkNotNull(delegate);
        this.maxRetries = maxRetries;
        Preconditions.checkArgument(this.maxRetries >= 0, "Negative retries");
    }

    @Override
    public Observable<RESPONSE> write(final RSRC newValue) {
        final ReadableResource<RESPONSE> readable = FluentWritableResource.from(this.delegateSetter).asReadableResource(newValue);
        final ReadableResource<RESPONSE> retryable = new RetryReadableResource<RESPONSE>(readable, this.maxRetries);
        final Observable<RESPONSE> response = retryable.get();
        return response;
    }

}
