package rickbw.crud.sync;

import java.io.IOException;


public interface SyncResourceUpdater<UPDATE, RESPONSE> {

    public abstract RESPONSE updateSync(UPDATE update) throws IOException;

}
