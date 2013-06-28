package rickbw.crud.sync;

import java.io.IOException;


public interface SyncResourceDeleter<RESPONSE> {

    public abstract RESPONSE deleteSync() throws IOException;

}
