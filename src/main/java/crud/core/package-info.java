/**
 * Contains a basic middleware abstraction.
 *
 * <h3>Idioms</h3>
 * <code><b>Observable&lt;Void&gt;</b></code>: Indicates an operation that may
 * operate asynchronously, emits no data on
 * {@link rx.Observer#onCompleted() success}, and emits a
 * {@link java.lang.Throwable} on
 * {@link rx.Observer#onError(Throwable) error}.
 *
 * @author Rick Warren
 */
package crud.core;
