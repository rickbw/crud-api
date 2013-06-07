package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.MapResourceConsumer;
import rickbw.crud.MapResourceProvider;
import rickbw.crud.async.AsyncMapResourceProvider;


public final class AsynchronizedMapResourceProvider<KEY, RSRC>
implements MapResourceProvider<KEY, RSRC> {

    private static final Logger log = LoggerFactory.getLogger(AsynchronizedMapResourceProvider.class);

    private final AsyncMapResourceProvider<KEY, RSRC> delegate;
    private final MapResourceConsumer<? super KEY, ? super Throwable> exceptionConsumer;
    private final Executor consumerExecutor;


    public AsynchronizedMapResourceProvider(
            final AsyncMapResourceProvider<KEY, RSRC> delegate,
            final Executor consumerExecutor,
            final Optional<MapResourceConsumer<? super KEY, ? super Throwable>> exceptionConsumer) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.consumerExecutor = Preconditions.checkNotNull(consumerExecutor);

        if (exceptionConsumer.isPresent()) {
            this.exceptionConsumer = exceptionConsumer.get();
        } else {
            this.exceptionConsumer = new MapResourceConsumer<KEY, Throwable>() {
                @Override
                public void accept(final KEY key, final Throwable value) {
                    log.error("Error occurred while getting " + key, value);
                }
            };
        }
        assert null != this.exceptionConsumer;
    }

    @Override
    public final void get(final KEY key, final MapResourceConsumer<? super KEY, ? super RSRC> consumer) {
        final ListenableFuture<RSRC> future = this.delegate.getAsync(key);
        Futures.addCallback(
                future,
                new Callback(consumer, key),
                this.consumerExecutor);
    }

    private final class Callback implements FutureCallback<RSRC> {
        private final KEY key;
        private final MapResourceConsumer<? super KEY, ? super RSRC> consumer;

        private Callback(final MapResourceConsumer<? super KEY, ? super RSRC> consumer, final KEY key) {
            this.key = key;
            this.consumer = Preconditions.checkNotNull(consumer);
        }

        @Override
        public void onSuccess(final RSRC result) {
            try {
                this.consumer.accept(this.key, result);
            } catch (final Throwable ex) {
                handleException(ex);
            } finally {
                try {
                    delegate.close(result);
                } catch (final IOException iox) {
                    log.error("Error closing " + result, iox);
                }
            }
        }

        @Override
        public void onFailure(final Throwable ex) {
            handleException(ex);
        }

        private void handleException(final Throwable ex) {
            try {
                exceptionConsumer.accept(this.key, ex);
            } catch (final Throwable th) {
                log.error("Error handling original exception " + ex, th);
            }
        }
    }

}
