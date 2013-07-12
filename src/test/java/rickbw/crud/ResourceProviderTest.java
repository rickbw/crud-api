package rickbw.crud;

import org.junit.Test;


/**
 * Just make sure this thing keeps compiling.
 */
public final class ResourceProviderTest {

    public interface Readable extends ReadableResource<String> {
        // empty
    }

    public interface Writable extends WritableResource<Object, StringBuilder> {
        // empty
    }

    public interface ReadWritable extends Readable, Writable {
        // empty
    }

    public interface Provider extends ReadableResourceProvider<Enum<?>, String>, WritableResourceProvider<Enum<?>, Object, StringBuilder> {
        @Override
        public abstract ReadWritable get(Enum<?> key);
    }

    @Test
    public void testNothing() {
        // nothing to do
    }

}
