/**
 * Contains a basic middleware abstraction.
 *
 * <h3>Idioms and Conventions</h3>
 * <code><b>Observable&lt;Void&gt;</b></code>: Indicates an operation that may
 * operate asynchronously, emits no data on
 * {@link rx.Observer#onCompleted() success}, and emits a
 * {@link java.lang.Throwable} on
 * {@link rx.Observer#onError(Throwable) error}.
 * <p/>
 * <b>Nullability</b> is generally indicated explicitly with the
 * {@link javax.annotation.Nonnull} and {@link javax.annotation.Nullable}
 * annotations. When not indicated, assume the value is <em>not</em> nullable.
 * In particular, types that already encapsulate multiplicity -- such as
 * {@link com.google.common.base.Optional}, {@link java.util.Collection} and
 * its subtypes, and {@link rx.Observable} -- are not generally explicitly
 * annotated, and are never considered nullable.
 *
 * @author Rick Warren
 */
package crud.core;
