package rickbw.crud.adapter;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.async.AsyncMapResourceDeleter;
import rickbw.retry.RetrierFuture;


public final class RetryAsyncMapResourceDeleter<KEY, RESPONSE>
implements AsyncMapResourceDeleter<KEY, RESPONSE> {

    private final AsyncMapResourceDeleter<? super KEY, RESPONSE> delegateDeleter;
    private final int maxRetries;


    public RetryAsyncMapResourceDeleter(
            final AsyncMapResourceDeleter<? super KEY, RESPONSE> delegate,
            final int maxRetries) {
        this.delegateDeleter = Preconditions.checkNotNull(delegate);

        Preconditions.checkArgument(maxRetries >= 0, "Negative retries");
        this.maxRetries = maxRetries;
    }

    @Override
    public ListenableFuture<RESPONSE> deleteAsync(final KEY key) {
        // Make first getFuture() call here so async activity starts now:
        final ListenableFuture<RESPONSE> future = this.delegateDeleter.deleteAsync(key);
        // Wrap in retrier in case original Future throws:
        final ListenableFuture<RESPONSE> retryFuture = new RetrierFuture<RESPONSE>(future, new FutureProvider(key), this.maxRetries);
        return retryFuture;
    }


    private final class FutureProvider implements Function<Integer, ListenableFuture<RESPONSE>> {
        @Nullable
        private final KEY key;

        public FutureProvider(@Nullable final KEY key) {
            this.key = key;
        }

        @Override
        public ListenableFuture<RESPONSE> apply(final Integer retryIndex) {
            return RetryAsyncMapResourceDeleter.this.delegateDeleter.deleteAsync(this.key);
        }
    }

}
