package rickbw.crud.sync;


public interface SyncUpdatableResourceProvider<KEY, UPDATE, RESPONSE> {

    public abstract SyncUpdatableResource<UPDATE, RESPONSE> get(KEY key);

}
