package rickbw.crud.async;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.retry.RetrierFuture;


public final class RetryAsyncMapResourceProvider<KEY, RSRC>
implements AsyncMapResourceProvider<KEY, RSRC> {

    private final AsyncMapResourceProvider<? super KEY, RSRC> delegateProvider;
    private final int maxRetries;


    public RetryAsyncMapResourceProvider(
            final AsyncMapResourceProvider<? super KEY, RSRC> delegate,
            final int maxRetries) {
        this.delegateProvider = Preconditions.checkNotNull(delegate);

        Preconditions.checkArgument(maxRetries >= 0, "Negative retries");
        this.maxRetries = maxRetries;
    }

    @Override
    public ListenableFuture<RSRC> getAsync(final KEY key) {
        // Make first getFuture() call here so async activity starts now:
        final ListenableFuture<RSRC> future = this.delegateProvider.getAsync(key);
        // Wrap in retrier in case original Future throws:
        final ListenableFuture<RSRC> retryFuture = new RetrierFuture<RSRC>(future, new FutureProvider(key), this.maxRetries);
        return retryFuture;
    }

    @Override
    public void close(final RSRC resource) throws IOException {
        this.delegateProvider.close(resource);
    }


    private final class FutureProvider implements Function<Integer, ListenableFuture<RSRC>> {
        @Nullable
        private final KEY key;

        public FutureProvider(@Nullable final KEY key) {
            this.key = Preconditions.checkNotNull(key);
        }

        @Override
        public ListenableFuture<RSRC> apply(final Integer retryIndex) {
            return RetryAsyncMapResourceProvider.this.delegateProvider.getAsync(this.key);
        }
    }

}
