package rickbw.crud.adapter;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import rickbw.crud.async.AsyncMapResourceSetter;
import rickbw.crud.sync.SyncMapResourceSetter;


public final class DeferredMapResourceSetter<KEY, UPDATE, RESPONSE>
extends DeferredResourceCloser<RESPONSE>
implements AsyncMapResourceSetter<KEY, UPDATE, RESPONSE> {

    private final SyncMapResourceSetter<? super KEY, ? super UPDATE, RESPONSE> delegate;
    private final ListeningExecutorService executor;


    public DeferredMapResourceSetter(
            final SyncMapResourceSetter<? super KEY, ? super UPDATE, RESPONSE> delegate,
            final ListeningExecutorService executor) {
        super(delegate);
        this.delegate = Preconditions.checkNotNull(delegate);
        this.executor = Preconditions.checkNotNull(executor);
    }

    @Override
    public ListenableFuture<RESPONSE> putAsync(final KEY key, final UPDATE update) {
        final ListenableFuture<RESPONSE> future = this.executor.submit(new Callable<RESPONSE>() {
            @Override
            public RESPONSE call() throws IOException {
                return delegate.putSync(key, update);
            }
        });
        final ListenableFuture<RESPONSE> safeFuture = wrap(future);
        return safeFuture;
    }

}
