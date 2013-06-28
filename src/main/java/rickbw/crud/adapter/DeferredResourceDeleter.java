package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncResourceDeleter;
import rickbw.crud.sync.SyncResourceDeleter;


public final class DeferredResourceDeleter<RESPONSE>
implements AsyncResourceDeleter<RESPONSE> {

    private final SyncResourceDeleter<RESPONSE> delegate;
    private final ListeningExecutorService executor;

    private final Callable<RESPONSE> task = new Callable<RESPONSE>() {
        @Override
        public RESPONSE call() throws IOException {
            return delegate.deleteSync();
        }
    };


    public DeferredResourceDeleter(
            final SyncResourceDeleter<RESPONSE> delegate,
            final ListeningExecutorService executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> deleteAsync() {
        final ListenableFuture<RESPONSE> future = this.executor.submit(this.task);
        return future;
    }

}
