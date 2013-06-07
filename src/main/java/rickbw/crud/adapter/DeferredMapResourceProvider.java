package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncMapResourceProvider;
import rickbw.crud.sync.SyncMapResourceProvider;


public final class DeferredMapResourceProvider<KEY, RSRC>
extends DeferredResourceCloser<RSRC>
implements AsyncMapResourceProvider<KEY, RSRC> {

    private final SyncMapResourceProvider<? super KEY, RSRC> delegate;
    private final ListeningExecutorService executor;


    public DeferredMapResourceProvider(
            final SyncMapResourceProvider<? super KEY, RSRC> delegate,
            final ListeningExecutorService executor) {
        super(delegate);
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RSRC> getAsync(final KEY key) {
        final ListenableFuture<RSRC> future = this.executor.submit(new Callable<RSRC>() {
            @Override
            public RSRC call() throws IOException {
                return delegate.getSync(key);
            }
        });
        final ListenableFuture<RSRC> safeFuture = wrap(future);
        return safeFuture;
    }

}
