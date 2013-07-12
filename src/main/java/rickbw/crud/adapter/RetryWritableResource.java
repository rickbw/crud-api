package rickbw.crud.adapter;

import com.google.common.base.Preconditions;

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
    public Observable<RESPONSE> write(final RSRC resource) {
        final ReadableResource<RESPONSE> readable = Resources.asReadableResource(this.delegateSetter, resource);
        final ReadableResource<RESPONSE> retryable = new RetryReadableResource<RESPONSE>(readable, this.maxRetries);
        final Observable<RESPONSE> response = retryable.get();
        return response;
    }

}
