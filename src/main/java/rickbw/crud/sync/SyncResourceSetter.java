package rickbw.crud.sync;

import java.io.IOException;


public interface SyncResourceSetter<UPDATE, RESPONSE> extends ResourceCloser<RESPONSE> {

    public abstract RESPONSE putSync(UPDATE update) throws IOException;

}
