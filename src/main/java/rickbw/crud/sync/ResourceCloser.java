package rickbw.crud.sync;

import java.io.IOException;


public interface ResourceCloser<RSRC> {

    public abstract void close(RSRC resource) throws IOException;

}
