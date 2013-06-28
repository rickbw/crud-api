package rickbw.crud.sync;

import java.io.IOException;


public interface SyncMapResourceSetter<KEY, RSRC, RESPONSE> {

    public abstract RESPONSE putSync(KEY key, RSRC update) throws IOException;

}
