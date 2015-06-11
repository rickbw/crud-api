/**
 * This package supports data-oriented interactions with generic
 * {@link crud.core.Resource}s, which encapsulate
 * state. The design emphasizes generality, safety and concurrency.
 *
 * <ul>
 *  <li><em>Generality</em>: The available interactions consist of the
 *  conventional "CRUD": Create, Read, Update, and Delete. These have an HTTP-
 *  like flare -- set, get, update, delete -- though the types in this package
 *  do <em>not</em> depend on HTTP as an implementation technology. (For an
 *  HTTP implementation, see the Crud HTTP project. These operations
 *  should also be familiar to anyone who has worked with other data-oriented
 *  APIs, such as JDBC.)</li>
 *
 *  <li><em>Safety</em>: The types of resources are statically type-safe,
 *  configured by means of generic type parameters. And because not all
 *  resources support all operations, the operations are composable, defined
 *  in separate interfaces designed to work together. For example, a resource
 *  that supports reading and writing, but not deletion, would implement
 *  {@link crud.core.ReadableResource} and
 *  {@link crud.core.WritableResource} but not
 *  {@link crud.core.DeletableResource}.</li>
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
 * {@link crud.core.ResourceProvider}s. The former encapsulates the
 * I/O operations on state, and hence uses a reactive style. There are four
 * derived interfaces, one for each CRUD operation:
 * <ul>
 *  <li>{@link crud.core.ReadableResource}</li>
 *  <li>{@link crud.core.WritableResource}</li>
 *  <li>{@link crud.core.UpdatableResource}</li>
 *  <li>{@link crud.core.DeletableResource}</li>
 * </ul>
 *
 * The latter abstraction, the resource providers, provide local (i.e.
 * assumed-cheap) navigation among Resources. This navigation uses a key-value
 * lookup idiom, where keys are generic and may be simple -- e.g. a URL -- or
 * arbitrarily complex -- e.g. a database query -- depending on the underlying
 * data-access technology. There are four derived
 * {@link crud.core.ResourceProvider} interfaces:
 * <ul>
 *  <li>{@link crud.core.ReadableResourceProvider}</li>
 *  <li>{@link crud.core.WritableResourceProvider}</li>
 *  <li>{@link crud.core.UpdatableResourceProvider}</li>
 *  <li>{@link crud.core.DeletableResourceProvider}</li>
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
