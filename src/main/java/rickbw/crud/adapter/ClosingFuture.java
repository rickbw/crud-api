package rickbw.crud.adapter;

import java.util.concurrent.ExecutionException;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ForwardingListenableFuture;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.future.FutureMapResourceProvider;


/**
 * A {@link ListenableFuture} for use with {@link FutureMapResourceProvider}
 * that makes sure results get closed properly.
 */
public final class ClosingFuture<V> extends ForwardingListenableFuture<V> {

    private final ListenableFuture<V> delegate;
    private final Function<? super V, ?> closer;


    public ClosingFuture(
            final ListenableFuture<V> delegate,
            final Function<? super V, ?> closer) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.closer = Preconditions.checkNotNull(closer);
    }

    /**
     * If the delegate {@link ListenableFuture} was not cancelled because it
     * has already completed, attempt to close it with
     * {@link FutureMapResourceProvider#close(Object)}. Failures to close will
     * be logged.
     */
    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        final boolean cancelled = this.delegate.cancel(mayInterruptIfRunning);
        if (!cancelled && this.delegate.isDone()) {
            try {
                final V result = this.delegate.get();
                this.closer.apply(result); // ignore return result
            } catch (final ExecutionException noop) {
                // No result, so nothing to close
            } catch (final InterruptedException unreachable) {
                // Since we already checked isDone()
                throw new AssertionError(unreachable);
            }
        }
        return cancelled;
    }

    @Override
    protected synchronized ListenableFuture<V> delegate() {
        return this.delegate;
    }

}
