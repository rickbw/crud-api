package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncUpdatableResource;
import rickbw.crud.sync.SyncUpdatableResource;


public final class DeferredUpdatableResource<UPDATE, RESPONSE>
implements AsyncUpdatableResource<UPDATE, RESPONSE> {

    private final SyncUpdatableResource<? super UPDATE, RESPONSE> delegate;
    private final ListeningExecutorService executor;


    public DeferredUpdatableResource(
            final SyncUpdatableResource<? super UPDATE, RESPONSE> delegate,
            final ListeningExecutorService executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> updateAsync(final UPDATE update) {
        final ListenableFuture<RESPONSE> future = this.executor.submit(new Callable<RESPONSE>() {
            @Override
            public RESPONSE call() throws IOException {
                return delegate.updateSync(update);
            }
        });
        return future;
    }

}
