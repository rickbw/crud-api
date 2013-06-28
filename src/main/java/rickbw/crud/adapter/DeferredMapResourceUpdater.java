package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncMapResourceUpdater;
import rickbw.crud.sync.SyncMapResourceUpdater;


public final class DeferredMapResourceUpdater<KEY, UPDATE, RESPONSE>
implements AsyncMapResourceUpdater<KEY, UPDATE, RESPONSE> {

    private final SyncMapResourceUpdater<? super KEY, ? super UPDATE, RESPONSE> delegate;
    private final ListeningExecutorService executor;


    public DeferredMapResourceUpdater(
            final SyncMapResourceUpdater<? super KEY, ? super UPDATE, RESPONSE> delegate,
            final ListeningExecutorService executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> updateAsync(final KEY key, final UPDATE update) {
        final ListenableFuture<RESPONSE> future = this.executor.submit(new Callable<RESPONSE>() {
            @Override
            public RESPONSE call() throws IOException {
                return delegate.updateSync(key, update);
            }
        });
        return future;
    }

}
