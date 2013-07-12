package rickbw.crud;



public interface DeletableResourceProvider<KEY, RESPONSE> {

    public abstract DeletableResource<RESPONSE> get(KEY key);

}
