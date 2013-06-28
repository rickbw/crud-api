package rickbw.crud.sync;


public interface SyncReadableResourceProvider<KEY, RSRC> {

    public abstract SyncReadableResource<RSRC> get(KEY key);

}
