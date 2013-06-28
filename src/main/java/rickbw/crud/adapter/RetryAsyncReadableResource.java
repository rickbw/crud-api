package rickbw.crud.adapter;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.async.AsyncReadableResource;
import rickbw.retry.RetrierFuture;


public final class RetryAsyncReadableResource<RSRC>
implements AsyncReadableResource<RSRC> {

    private final AsyncReadableResource<RSRC> delegateProvider;
    private final int maxRetries;

    private final Function<Integer, ListenableFuture<RSRC>> futureProvider =
            new Function<Integer, ListenableFuture<RSRC>>() {
                @Override
                public ListenableFuture<RSRC> apply(final Integer retryIndex) {
                    return RetryAsyncReadableResource.this.delegateProvider.getAsync();
                }
            };


    public RetryAsyncReadableResource(
            final AsyncReadableResource<RSRC> delegate,
            final int maxRetries) {
        this.delegateProvider = Preconditions.checkNotNull(delegate);

        Preconditions.checkArgument(maxRetries >= 0, "Negative retries");
        this.maxRetries = maxRetries;
    }

    @Override
    public ListenableFuture<RSRC> getAsync() {
        // Make first getFuture() call here so async activity starts now:
        final ListenableFuture<RSRC> future = this.delegateProvider.getAsync();
        final ListenableFuture<RSRC> retryFuture = new RetrierFuture<RSRC>(future, futureProvider, this.maxRetries);
        return retryFuture;
    }

}
