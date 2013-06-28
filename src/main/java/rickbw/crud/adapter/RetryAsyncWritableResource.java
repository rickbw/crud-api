package rickbw.crud.adapter;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.async.AsyncWritableResource;
import rickbw.retry.RetrierFuture;


public final class RetryAsyncWritableResource<RSRC, RESPONSE>
implements AsyncWritableResource<RSRC, RESPONSE> {

    private final AsyncWritableResource<? super RSRC, RESPONSE> delegateSetter;
    private final int maxRetries;


    public RetryAsyncWritableResource(
            final AsyncWritableResource<? super RSRC, RESPONSE> delegate,
            final int maxRetries) {
        this.delegateSetter = Preconditions.checkNotNull(delegate);

        Preconditions.checkArgument(maxRetries >= 0, "Negative retries");
        this.maxRetries = maxRetries;
    }

    @Override
    public ListenableFuture<RESPONSE> writeAsync(final RSRC resource) {
        // Make first getFuture() call here so async activity starts now:
        final ListenableFuture<RESPONSE> future = this.delegateSetter.writeAsync(resource);
        // Wrap in retrier in case original Future throws:
        final ListenableFuture<RESPONSE> retryFuture = new RetrierFuture<RESPONSE>(future, new FutureSetter(resource), this.maxRetries);
        return retryFuture;
    }


    private final class FutureSetter implements Function<Integer, ListenableFuture<RESPONSE>> {
        @Nullable
        private final RSRC resource;

        public FutureSetter(@Nullable final RSRC resource) {
            this.resource = resource;
        }

        @Override
        public ListenableFuture<RESPONSE> apply(final Integer retryIndex) {
            return RetryAsyncWritableResource.this.delegateSetter.writeAsync(this.resource);
        }
    }

}
