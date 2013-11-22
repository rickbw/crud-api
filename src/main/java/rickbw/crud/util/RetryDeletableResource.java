package rickbw.crud.util;

import rickbw.crud.DeletableResource;
import rickbw.crud.ReadableResource;
import com.google.common.base.Preconditions;

import rx.Observable;


public final class RetryDeletableResource<RESPONSE>
implements DeletableResource<RESPONSE> {

    private final ReadableResource<RESPONSE> readableDelegate;
    private final int maxRetries;


    public RetryDeletableResource(
            final DeletableResource<RESPONSE> delegate,
            final int maxRetries) {
        this.readableDelegate = DeletableResources.asReadableResource(delegate);
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
