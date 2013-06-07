package rickbw.crud.adapter;

import java.io.Closeable;
import java.io.IOException;

import rickbw.crud.sync.ResourceCloser;


public class CloseableCloser implements ResourceCloser<Closeable> {

    @Override
    public final void close(final Closeable resource) throws IOException {
        resource.close();
    }

}
