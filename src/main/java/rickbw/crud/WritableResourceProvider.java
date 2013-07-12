package rickbw.crud;



public interface WritableResourceProvider<KEY, RSRC, RESPONSE> {

    public abstract WritableResource<RSRC, RESPONSE> get(KEY key);

}
