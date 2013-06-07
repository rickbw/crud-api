package rickbw.crud.sync;

import java.io.IOException;


public interface SyncMapResourceDeleter<KEY, RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract RESPONSE deleteSync(KEY key) throws IOException;

}
