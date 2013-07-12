package rickbw.crud;



public interface UpdatableResourceProvider<KEY, UPDATE, RESPONSE> {

    public abstract UpdatableResource<UPDATE, RESPONSE> get(KEY key);

}
