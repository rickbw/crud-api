package rickbw.crud.sync;

import java.io.IOException;


public interface SyncWritableResource<RSRC, RESPONSE> {

    public abstract RESPONSE writeSync(RSRC update) throws IOException;

}
