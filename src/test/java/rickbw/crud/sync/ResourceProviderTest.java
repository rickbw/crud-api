package rickbw.crud.sync;

import org.junit.Test;


/**
 * Just make sure this thing keeps compiling.
 */
public final class ResourceProviderTest {

    public interface Readable extends SyncReadableResource<String> {
        // empty
    }

    public interface Writable extends SyncWritableResource<Object, StringBuilder> {
        // empty
    }

    public interface ReadWritable extends Readable, Writable {
        // empty
    }

    public interface Provider extends SyncReadableResourceProvider<Enum<?>, String>, SyncWritableResourceProvider<Enum<?>, Object, StringBuilder> {
        @Override
        public abstract ReadWritable get(Enum<?> key);
    }

    @Test
    public void testNothing() {
        // nothing to do
    }

}
