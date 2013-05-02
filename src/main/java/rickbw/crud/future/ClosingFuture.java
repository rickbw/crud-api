package rickbw.crud.future;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;


/**
 * A {@link ListenableFuture} for use with {@link FutureMapResourceProvider}
 * that makes sure results get closed propery.
 */
public class ClosingFuture<V> implements ListenableFuture<V> {

    private static final Logger log = LoggerFactory.getLogger(ClosingFuture.class);

    private final ListenableFuture<? extends V> delegate;
    private final FutureMapResourceProvider<?, ? super V> closer;


    public ClosingFuture(
            final ListenableFuture<? extends V> delegate,
            final FutureMapResourceProvider<?, ? super V> closer) {
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
                try {
                    this.closer.close(result);
                } catch (final IOException iox) {
                    log.error("Unable to close " + result, iox);
                }
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
    public boolean isCancelled() {
        return this.delegate.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.delegate.isDone();
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {
        return this.delegate.get();
    }

    @Override
    public V get(final long timeout, final TimeUnit unit)
    throws InterruptedException, ExecutionException, TimeoutException {
        return this.delegate.get(timeout, unit);
    }

    @Override
    public void addListener(final Runnable listener, final Executor executor) {
        this.delegate.addListener(listener, executor);
    }

}
