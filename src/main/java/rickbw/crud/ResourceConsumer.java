package rickbw.crud;


/**
 * A consumer of resources of uniform type.
 *
 * @param <RSRC>    The type of the resources.
 */
public interface ResourceConsumer<RSRC> {

    public abstract void accept(RSRC value);

}
