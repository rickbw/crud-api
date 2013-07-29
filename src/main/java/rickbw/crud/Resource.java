package rickbw.crud;


/**
 * A "resource" encapsulates a particular state. This is a marker interface
 * for all other resource interfaces.
 */
public interface Resource {

    /**
     * A resource is considered to have an intrinsic identity. Two resource
     * objects are considered to be equal if they have the same identity.
     * For example, two resources available via HTTP would be equal if they
     * point to the same URI. Equality should <em>not</em> require performing
     * expensive value retrievals and comparing the results.
     */
    @Override
    public abstract boolean equals(Object other);

    @Override
    public abstract int hashCode();

}
