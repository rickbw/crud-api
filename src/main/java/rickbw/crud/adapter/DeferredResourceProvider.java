package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncResourceProvider;
import rickbw.crud.sync.SyncResourceProvider;


public final class DeferredResourceProvider<RSRC>
implements AsyncResourceProvider<RSRC> {

    private final SyncResourceProvider<RSRC> delegate;
    private final ListeningExecutorService executor;

    private final Callable<RSRC> task = new Callable<RSRC>() {
        @Override
        public RSRC call() throws IOException {
            return delegate.getSync();
        }
    };


    public DeferredResourceProvider(
            final SyncResourceProvider<RSRC> delegate,
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
