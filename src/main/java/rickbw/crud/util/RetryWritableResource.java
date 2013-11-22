package rickbw.crud.util;

import rickbw.crud.ReadableResource;
import rickbw.crud.WritableResource;
import com.google.common.base.Preconditions;

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
