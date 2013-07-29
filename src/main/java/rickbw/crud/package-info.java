/**
 * This package supports data-oriented interactions with generic
 * {@link rickbw.crud.Resource}s, which encapsulate
 * state. The design emphasizes generality, safety and concurrency.
 *
 * <ul>
 *  <li><em>Generality</em>: The available interactions consist of the
 *  conventional "CRUD": Create, Read, Update, and Delete. These have an HTTP-
 *  like flare -- set, get, update, delete -- though the types in this package
 *  do <em>not</em> depend on HTTP as an implementation technology. (For an
 *  HTTP implementation, see
 *  {@link rickbw.crud.http}. These operations
 *  should also be familiar to anyone who has worked with other data-oriented
 *  APIs, such as JDBC.) Each of these capabilities is represented by a
 *  distinct interface; see e.g.
 *  {@link rickbw.crud.ReadableResource}. Resource
 *  navigation is also represented by distinct interfaces; see e.g.
 *  {@link rickbw.crud.ReadableResourceProvider}.
 *  </li>
 *
 *  <li><em>Safety</em>: The types of resources are statically type-safe,
 *  configured by means of generic type parameters. And because not all
 *  resources support all operations, the operations are composable, defined
 *  in separate interfaces designed to work together. For example, a resource
 *  that supports reading and writing, but not deletion, would implement
 *  {@link rickbw.crud.ReadableResource} and
 *  {@link rickbw.crud.WritableResource} but not
 *  {@link rickbw.crud.DeletableResource}.</li>
 *
 *  <li><em>Concurrency</em>: The API encourages asynchronous implementations.
 *  It encapsulates asynchrony using the Rx-Java and its {@link rx.Observable}
 *  class. This encapsulation means that applications can work with
 *  asynchronous implementations just as easily as synchronous ones, and
 *  cross-cutting behaviors like retries can be transparently composed as
 *  needed -- see, for example,
 *  {@link rickbw.crud.adapter.RetryReadableResource}.
 *  </li>
 * </ul>
 */
package rickbw.crud;
