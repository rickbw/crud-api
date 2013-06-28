package rickbw.crud.adapter;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.async.AsyncMapResourceSetter;
import rickbw.retry.RetrierFuture;


public final class RetryAsyncMapResourceSetter<KEY, RSRC, RESPONSE>
implements AsyncMapResourceSetter<KEY, RSRC, RESPONSE> {

    private final AsyncMapResourceSetter<? super KEY, ? super RSRC, RESPONSE> delegateSetter;
    private final int maxRetries;


    public RetryAsyncMapResourceSetter(
            final AsyncMapResourceSetter<? super KEY, ? super RSRC, RESPONSE> delegate,
            final int maxRetries) {
        this.delegateSetter = Preconditions.checkNotNull(delegate);

        Preconditions.checkArgument(maxRetries >= 0, "Negative retries");
        this.maxRetries = maxRetries;
    }

    @Override
    public ListenableFuture<RESPONSE> putAsync(final KEY key, final RSRC resource) {
        // Make first getFuture() call here so async activity starts now:
        final ListenableFuture<RESPONSE> future = this.delegateSetter.putAsync(key, resource);
        // Wrap in retrier in case original Future throws:
        final ListenableFuture<RESPONSE> retryFuture = new RetrierFuture<RESPONSE>(future, new FutureSetter(key, resource), this.maxRetries);
        return retryFuture;
    }


    private final class FutureSetter implements Function<Integer, ListenableFuture<RESPONSE>> {
        @Nullable
        private final KEY key;
        @Nullable
        private final RSRC resource;

        public FutureSetter(@Nullable final KEY key, @Nullable final RSRC resource) {
            this.key = key;
            this.resource = resource;
        }

        @Override
        public ListenableFuture<RESPONSE> apply(final Integer retryIndex) {
            return RetryAsyncMapResourceSetter.this.delegateSetter.putAsync(this.key, this.resource);
        }
    }

}
