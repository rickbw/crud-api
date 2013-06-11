package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncResourceSetter;
import rickbw.crud.sync.SyncResourceSetter;


public final class DeferredResourceSetter<UPDATE, RESPONSE>
extends DeferredResourceCloser<RESPONSE>
implements AsyncResourceSetter<UPDATE, RESPONSE> {

    private final SyncResourceSetter<? super UPDATE, RESPONSE> delegate;
    private final ListeningExecutorService executor;


    public DeferredResourceSetter(
            final SyncResourceSetter<? super UPDATE, RESPONSE> delegate,
            final ListeningExecutorService executor) {
        super(delegate);
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> putAsync(final UPDATE update) {
        final ListenableFuture<RESPONSE> future = this.executor.submit(new Callable<RESPONSE>() {
            @Override
            public RESPONSE call() throws IOException {
                return delegate.putSync(update);
            }
        });
        final ListenableFuture<RESPONSE> safeFuture = wrap(future);
        return safeFuture;
    }

}
