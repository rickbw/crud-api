package rickbw.crud;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import rickbw.crud.sync.SyncMapResourceProvider;


public final class AsynchronizedMapResourceProvider<KEY, RSRC>
implements MapResourceProvider<KEY, RSRC> {

    private static final Logger log = LoggerFactory.getLogger(AsynchronizedMapResourceProvider.class);

    private final SyncMapResourceProvider<KEY, RSRC> delegate;
    private final MapResourceConsumer<? super KEY, ? super Exception> exceptionConsumer;
    private final Executor providerExecutor;
    private final Executor consumerExecutor;


    public AsynchronizedMapResourceProvider(
            final SyncMapResourceProvider<KEY, RSRC> delegate,
            final Executor providerExecutor,
            final Executor consumerExecutor,
            final Optional<MapResourceConsumer<? super KEY, ? super Exception>> exceptionConsumer) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.providerExecutor = Preconditions.checkNotNull(providerExecutor);
        this.consumerExecutor = Preconditions.checkNotNull(consumerExecutor);

        if (exceptionConsumer.isPresent()) {
            this.exceptionConsumer = exceptionConsumer.get();
        } else {
            this.exceptionConsumer = new MapResourceConsumer<KEY, Exception>() {
                @Override
                public void accept(final KEY key, final Exception value) {
                    log.error("Error occurred while getting " + key, value);
                }
            };
        }
        assert null != this.exceptionConsumer;
    }

    @Override
    public final void get(final KEY key, final MapResourceConsumer<? super KEY, ? super RSRC> consumer) {
        this.providerExecutor.execute(new Task(key, consumer));
    }

    private final class Task implements Runnable {
        private final KEY key;
        private final MapResourceConsumer<? super KEY, ? super RSRC> consumer;

        private Task(final KEY key, final MapResourceConsumer<? super KEY, ? super RSRC> consumer) {
            this.key = key;
            this.consumer = Preconditions.checkNotNull(consumer);
        }

        @Override
        public void run() {
            try {
                final RSRC resource = delegate.get(this.key);
                final AtomicBoolean closed = new AtomicBoolean(false);
                try {
                    consumerExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                consumer.accept(key, resource);
                            } catch (final RuntimeException rex) {
                                handleException(rex);
                            } finally {
                                closed.set(true);
                                try {
                                    delegate.close(resource);
                                } catch (final IOException iox) {
                                    log.error("Error during close", iox);
                                }
                            }
                        }
                    });
                } catch (final Exception ex) {
                    /* If the Executor has been shut down, it may throw
                     * RejectedException. If it is synchronous, it may throw
                     * any RuntimeException from the task itself. And if it is
                     * synchronous, and we blindly close here, we could end up
                     * closing twice. However, if it is asynchronous, and no
                     * error occurs, we must not close here no matter what,
                     * or we will close before the task runs.
                     */
                    if (!closed.getAndSet(true)) {
                        delegate.close(resource);
                    }
                    throw ex;
                }
            } catch (final Exception ex) {
                handleException(ex);
            }
        }

        private void handleException(final Exception exception) {
            try {
                consumerExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            exceptionConsumer.accept(key, exception);
                        } catch (final RuntimeException rex) {
                            log.error("Error in exception consumer for " + exception,
                                      rex);
                        }
                    }
                });
            } catch (final RuntimeException rex) {
                log.error("Error dispatching exception consumer for " + exception +
                            " while getting " + key,
                          rex);
            }
        }
    }

}
