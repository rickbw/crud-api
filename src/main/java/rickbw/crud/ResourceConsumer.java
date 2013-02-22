package rickbw.crud;


/**
 * A user of data, where each data item is identified by a unique key. Keys
 * and values are both of uniform type.
 *
 * @param <K> The type of the keys
 * @param <V> The type of the values
 */
public interface ResourceConsumer<K, V> {

    public abstract void accept(K key, V value);

}
