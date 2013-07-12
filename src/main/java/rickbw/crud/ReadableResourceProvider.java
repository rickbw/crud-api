package rickbw.crud;



public interface ReadableResourceProvider<KEY, RSRC> {

    public abstract ReadableResource<RSRC> get(KEY key);

}
