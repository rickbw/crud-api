package rickbw.crud.adapter;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.async.AsyncDeletableResource;
import rickbw.retry.RetrierFuture;


public final class RetryAsyncDeletableResource<RESPONSE>
implements AsyncDeletableResource<RESPONSE> {

    private final AsyncDeletableResource<RESPONSE> delegateDeleter;
    private final int maxRetries;

    private final Function<Integer, ListenableFuture<RESPONSE>> futureProvider =
            new Function<Integer, ListenableFuture<RESPONSE>>() {
                @Override
                public ListenableFuture<RESPONSE> apply(final Integer retryIndex) {
                    return RetryAsyncDeletableResource.this.delegateDeleter.deleteAsync();
                }
            };


    public RetryAsyncDeletableResource(
            final AsyncDeletableResource<RESPONSE> delegate,
            final int maxRetries) {
        this.delegateDeleter = Preconditions.checkNotNull(delegate);

        Preconditions.checkArgument(maxRetries >= 0, "Negative retries");
        this.maxRetries = maxRetries;
    }

    @Override
    public ListenableFuture<RESPONSE> deleteAsync() {
        // Make first getFuture() call here so async activity starts now:
        final ListenableFuture<RESPONSE> future = this.delegateDeleter.deleteAsync();
        // Wrap in retrier in case original Future throws:
        final ListenableFuture<RESPONSE> retryFuture = new RetrierFuture<RESPONSE>(future, futureProvider, this.maxRetries);
        return retryFuture;
    }

}
