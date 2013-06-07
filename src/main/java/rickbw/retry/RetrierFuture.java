package rickbw.retry;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.memo.Memoizer;


/**
 * Returns the result of a given wrapped {@link ListenableFuture} if it
 * doesn't throw; otherwise, retry up to a given number of times on
 * {@link ExecutionException}. This class is guaranteed to retry no more
 * times than allowed, regardless of the number of (possible concurrent)
 * calls to {@link #get()} or {@link #get(long, TimeUnit)}.
 */
public final class RetrierFuture<RSRC> extends ForwardingListenableFuture<RSRC> {

    private final ListenableFuture<RSRC> delegateFuture;

    /**
     * Keep track of attempts so we don't retry too many times if
     * {@link #get()} (and/or overloads) are called multiple times.
     */
    private final Memoizer<Integer, ListenableFuture<RSRC>> retrier;

    private final int maxRetries;


    /**
     * @param delegateFuture    The initial "try", started by the caller. This
     *        Future's result will be returned if it doesn't throw.
     * @param futureProvider    Provides subsequent "retry" results as
     *        necessary. Retries will be numbered from 0 to maxRetries - 1.
     * @param maxRetries        The maximum number of times to get an object
     *        from the futureProvider.
     */
    public RetrierFuture(
            final ListenableFuture<RSRC> delegateFuture,
            final Function<Integer, ListenableFuture<RSRC>> futureProvider,
            final int maxRetries) {
        this.delegateFuture = Preconditions.checkNotNull(delegateFuture);
        this.retrier = new Memoizer<Integer, ListenableFuture<RSRC>>(futureProvider);
        this.maxRetries = maxRetries;
        Preconditions.checkArgument(this.maxRetries >= 0, "negative maxRetries");
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
            for (int i = 0; i < this.maxRetries; ++i) {
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
