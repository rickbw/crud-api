package rickbw.crud.sync;

import java.io.IOException;


public interface SyncResourceSetter<RSRC, RESPONSE> {

    public abstract RESPONSE putSync(RSRC update) throws IOException;

}
