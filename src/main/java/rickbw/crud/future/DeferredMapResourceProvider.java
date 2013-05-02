package rickbw.crud.future;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.sync.SyncMapResourceProvider;


public final class DeferredMapResourceProvider<KEY, RSRC>
implements FutureMapResourceProvider<KEY, RSRC> {

    private final SyncMapResourceProvider<? super KEY, RSRC> delegate;
    private final ListeningExecutorService executor;


    public DeferredMapResourceProvider(
            final SyncMapResourceProvider<? super KEY, RSRC> delegate,
            final ListeningExecutorService executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RSRC> getFuture(final KEY key) {
        final ListenableFuture<RSRC> future = this.executor.submit(new Callable<RSRC>() {
            @Override
            public RSRC call() throws Exception {
                return delegate.get(key);
            }
        });
        final ListenableFuture<RSRC> safeFuture = new ClosingFuture<RSRC>(future, this);
        return safeFuture;
    }

    @Override
    public void close(final RSRC resource) throws IOException {
        this.delegate.close(resource);
    }

}
