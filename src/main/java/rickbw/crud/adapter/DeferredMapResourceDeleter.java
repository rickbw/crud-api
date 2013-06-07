package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.future.FutureMapResourceDeleter;
import rickbw.crud.sync.SyncMapResourceDeleter;


public final class DeferredMapResourceDeleter<KEY, RESPONSE>
extends DeferredResourceCloser<RESPONSE>
implements FutureMapResourceDeleter<KEY, RESPONSE> {

    private final SyncMapResourceDeleter<? super KEY, RESPONSE> delegate;
    private final ListeningExecutorService executor;


    public DeferredMapResourceDeleter(
            final SyncMapResourceDeleter<? super KEY, RESPONSE> delegate,
            final ListeningExecutorService executor) {
        super(delegate);
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> deleteFuture(final KEY key) {
        final ListenableFuture<RESPONSE> future = this.executor.submit(new Callable<RESPONSE>() {
            @Override
            public RESPONSE call() throws IOException {
                return delegate.delete(key);
            }
        });
        final ListenableFuture<RESPONSE> safeFuture = wrap(future);
        return safeFuture;
    }

}
