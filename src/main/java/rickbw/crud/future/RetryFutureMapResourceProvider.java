package rickbw.crud.future;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.memo.Memoizer;


public final class RetryFutureMapResourceProvider<KEY, RSRC>
implements FutureMapResourceProvider<KEY, RSRC> {

    private final FutureMapResourceProvider<? super KEY, RSRC> delegateProvider;
    private final int maxRetries;


    public RetryFutureMapResourceProvider(
            final FutureMapResourceProvider<? super KEY, RSRC> delegate,
            final int maxRetries) {
        this.delegateProvider = Preconditions.checkNotNull(delegate);

        Preconditions.checkArgument(maxRetries >= 0, "Negative retries");
        this.maxRetries = maxRetries;
    }

    @Override
    public ListenableFuture<RSRC> getFuture(final KEY key) {
        // Make first getFuture() call here so async activity starts now:
        final ListenableFuture<RSRC> future = this.delegateProvider.getFuture(key);
        // Wrap in retrier in case original Future throws:
        final ListenableFuture<RSRC> retryFuture = new RetrierFuture(key, future);
        return retryFuture;
    }

    @Override
    public void close(final RSRC resource) throws IOException {
        this.delegateProvider.close(resource);
    }


    /**
     * Returns the result of a given wrapped {@link ListenableFuture} if it
     * doesn't throw; otherwise, retry up to a given number of times on
     * {@link ExecutionException}. This class is guaranteed to retry no more
     * times than allowed, regardless of the number of (possible concurrent)
     * calls to {@link #get()} or {@link #get(long, TimeUnit)}.
     */
    private final class RetrierFuture extends ForwardingListenableFuture<RSRC> {
        @Nullable
        private final KEY key;
        private volatile ListenableFuture<RSRC> delegateFuture;

        /**
         * Keep track of attempts so we don't retry too many times if
         * {@link #get()} (and/or overloads) are called multiple times.
         */
        private final Memoizer<Integer, ListenableFuture<RSRC>> retrier = new Memoizer<Integer, ListenableFuture<RSRC>>(
                new Function<Integer, ListenableFuture<RSRC>>() {
                    @Override
                    public ListenableFuture<RSRC> apply(final Integer retryIndex) {
                        return RetryFutureMapResourceProvider.this.delegateProvider.getFuture(RetrierFuture.this.key);
                    }
                });


        public RetrierFuture(@Nullable final KEY key, final ListenableFuture<RSRC> delegateFuture) {
            this.key = key; // not our business whether it's null
            this.delegateFuture = Preconditions.checkNotNull(delegateFuture);
        }

        @Override
        public RSRC get() throws InterruptedException, ExecutionException {
            try {
                return get(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
            } catch (final TimeoutException unreachable) {
                throw new AssertionError(unreachable);
            }
        }

        /**
         * This method won't block longer than the given amount of time,
         * regardless of the number of retries needed.
         */
        @Override
        public synchronized RSRC get(final long timeout, final TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
            final long startNanos = System.nanoTime();
            ExecutionException mostRecentFailure = null;

            try {
                // First "try":
                return delegate().get(timeout, unit);
            } catch (final ExecutionException ex) {
                mostRecentFailure = ex;

                // Subsequent "retries":
                for (int i = 0; i < RetryFutureMapResourceProvider.this.maxRetries; ++i) {
                    final ListenableFuture<RSRC> future = this.retrier.get(i);
                    final long elapsedNanos = System.nanoTime() - startNanos;
                    final long newTimeoutNanos = Math.max(unit.toNanos(timeout) - elapsedNanos, 0L);
                    try {
                        return future.get(newTimeoutNanos, TimeUnit.NANOSECONDS);
                    } catch (final ExecutionException retryEx) {
                        mostRecentFailure = retryEx;
                    }
                }
            }

            assert null != mostRecentFailure;
            throw mostRecentFailure;
        }

        @Override
        protected synchronized ListenableFuture<RSRC> delegate() {
            return this.delegateFuture;
        }
    }

}
