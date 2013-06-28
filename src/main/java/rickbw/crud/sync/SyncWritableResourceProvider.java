package rickbw.crud.sync;


public interface SyncWritableResourceProvider<KEY, RSRC, RESPONSE> {

    public abstract SyncWritableResource<RSRC, RESPONSE> get(KEY key);

}
