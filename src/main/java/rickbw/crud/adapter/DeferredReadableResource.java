package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncReadableResource;
import rickbw.crud.sync.SyncReadableResource;


public final class DeferredReadableResource<RSRC>
implements AsyncReadableResource<RSRC> {

    private final SyncReadableResource<RSRC> delegate;
    private final ListeningExecutorService executor;

    private final Callable<RSRC> task = new Callable<RSRC>() {
        @Override
        public RSRC call() throws IOException {
            return delegate.getSync();
        }
    };


    public DeferredReadableResource(
            final SyncReadableResource<RSRC> delegate,
            final ListeningExecutorService executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RSRC> getAsync() {
        final ListenableFuture<RSRC> future = this.executor.submit(this.task);
        return future;
    }

}
