package rickbw.crud.adapter;

import java.io.IOException;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;

import rickbw.crud.sync.ResourceCloser;


public class DeferredResourceCloser<RSRC> implements ResourceCloser<RSRC> {

    private final ResourceCloser<RSRC> delegate;


    public DeferredResourceCloser(final ResourceCloser<RSRC> delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    @Override
    public final void close(final RSRC resource) throws IOException {
        this.delegate.close(resource);
    }

    protected ClosingFuture<RSRC> wrap(final ListenableFuture<RSRC> future) {
        return new ClosingFuture<RSRC>(future, new Function<RSRC, Object>() {
            @Override
            public Object apply(@Nullable final RSRC input) {
                try {
                    close(input);
                    return null;
                } catch (final IOException iox) {
                    throw new IllegalStateException(iox);
                }
            }
        });
    }

}
