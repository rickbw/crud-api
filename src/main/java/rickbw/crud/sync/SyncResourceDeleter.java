package rickbw.crud.sync;

import java.io.IOException;


public interface SyncResourceDeleter<RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract RESPONSE deleteSync() throws IOException;

}
