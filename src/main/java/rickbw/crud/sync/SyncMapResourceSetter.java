package rickbw.crud.sync;

import java.io.IOException;


public interface SyncMapResourceSetter<KEY, UPDATE, RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract RESPONSE putSync(KEY key, UPDATE update) throws IOException;

}
