package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncMapResourceDeleter;
import rickbw.crud.sync.SyncMapResourceDeleter;


public final class DeferredMapResourceDeleter<KEY, RESPONSE>
implements AsyncMapResourceDeleter<KEY, RESPONSE> {

    private final SyncMapResourceDeleter<? super KEY, RESPONSE> delegate;
    private final ListeningExecutorService executor;


    public DeferredMapResourceDeleter(
            final SyncMapResourceDeleter<? super KEY, RESPONSE> delegate,
            final ListeningExecutorService executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> deleteAsync(final KEY key) {
        final ListenableFuture<RESPONSE> future = this.executor.submit(new Callable<RESPONSE>() {
            @Override
            public RESPONSE call() throws IOException {
                return delegate.deleteSync(key);
            }
        });
        return future;
    }

}
