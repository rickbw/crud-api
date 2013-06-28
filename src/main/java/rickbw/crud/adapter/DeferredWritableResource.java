package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncWritableResource;
import rickbw.crud.sync.SyncWritableResource;


public final class DeferredWritableResource<RSRC, RESPONSE>
implements AsyncWritableResource<RSRC, RESPONSE> {

    private final SyncWritableResource<? super RSRC, RESPONSE> delegate;
    private final ListeningExecutorService executor;


    public DeferredWritableResource(
            final SyncWritableResource<? super RSRC, RESPONSE> delegate,
            final ListeningExecutorService executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> writeAsync(final RSRC update) {
        final ListenableFuture<RESPONSE> future = this.executor.submit(new Callable<RESPONSE>() {
            @Override
            public RESPONSE call() throws IOException {
                return delegate.writeSync(update);
            }
        });
        return future;
    }

}
