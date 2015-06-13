/**
 * This package supports data-oriented interactions with generic
 * {@link crud.core.Resource}s, which encapsulate
 * state. The design emphasizes generality, safety and concurrency.
 *
 * <ul>
 *  <li><em>Generality</em>: The available interactions consist of the
 *  simple {@link crud.core.ReadableResource#read() read} and
 *  {@link crud.core.WritableResource#write(Object) write} operations. Writes
 *  may have slightly different semantics, depending on the underlying
 *  middleware implementation, e.g. update an existing record, replace an
 *  existing record, insert a new record, or send a message. This abstraction
 *  layer does not attempt to capture all possible semantics in the API.</li>
 *
 *  <li><em>Safety</em>: The types of resources are statically type-safe,
 *  configured by means of generic type parameters.</li>
 *
 *  <li><em>Concurrency</em>: The API encourages asynchronous implementations.
 *  It encapsulates asynchrony using Rx-Java and its {@link rx.Observable}
 *  class. This encapsulation means that applications can work with
 *  asynchronous implementations just as easily as synchronous ones, and
 *  cross-cutting behaviors like retries can be transparently composed as
 *  needed.
 *  </li>
 * </ul>
 *
 * There are two primary abstractions in the API:
 * {@link crud.core.Resource}s and
 * {@link crud.core.ResourceSet}s. The former encapsulates the
 * I/O operations on state, and hence uses a reactive style. There are two
 * derived interfaces:
 * <ul>
 *  <li>{@link crud.core.ReadableResource}</li>
 *  <li>{@link crud.core.WritableResource}</li>
 * </ul>
 *
 * The latter abstraction, the resource sets, provide local (i.e.
 * assumed-cheap) navigation among Resources. This navigation uses a key-value
 * lookup idiom, where keys are generic and may be simple -- e.g. a URL -- or
 * arbitrarily complex -- e.g. a database query -- depending on the underlying
 * data-access technology. There are two derived
 * {@link crud.core.ResourceSet} interfaces:
 * <ul>
 *  <li>{@link crud.core.ReadableResourceSet}</li>
 *  <li>{@link crud.core.WritableResourceSet}</li>
 * </ul>
 *
 * In addition to these core abstractions, this library provides a number of
 * utilities of two kinds in corresponding packages:
 * <ul>
 *  <li>The {@link crud.util} package contains general utilities.</li>
 *  <li>The {@link crud.pattern} package contains ready-to-
 *      implement combinations of Crud interfaces intended to meet the needs
 *      of certain data-access patterns out of the box.</li>
 * </li>
 */
package crud.core;
