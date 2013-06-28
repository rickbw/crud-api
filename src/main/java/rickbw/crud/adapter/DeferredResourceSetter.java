package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncResourceSetter;
import rickbw.crud.sync.SyncResourceSetter;


public final class DeferredResourceSetter<RSRC, RESPONSE>
implements AsyncResourceSetter<RSRC, RESPONSE> {

    private final SyncResourceSetter<? super RSRC, RESPONSE> delegate;
    private final ListeningExecutorService executor;


    public DeferredResourceSetter(
            final SyncResourceSetter<? super RSRC, RESPONSE> delegate,
            final ListeningExecutorService executor) {
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> putAsync(final RSRC update) {
        final ListenableFuture<RESPONSE> future = this.executor.submit(new Callable<RESPONSE>() {
            @Override
            public RESPONSE call() throws IOException {
                return delegate.putSync(update);
            }
        });
        return future;
    }

}
