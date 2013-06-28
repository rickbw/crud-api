package rickbw.crud.sync;

import java.io.IOException;


public interface SyncMapResourceDeleter<KEY, RESPONSE> {

    public abstract RESPONSE deleteSync(KEY key) throws IOException;

}
