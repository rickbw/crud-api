package rickbw.crud.sync;


public interface SyncDeletableResourceProvider<KEY, RESPONSE> {

    public abstract SyncDeletableResource<RESPONSE> get(KEY key);

}
