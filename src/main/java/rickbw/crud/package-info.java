/**
 * This package supports data-oriented interactions with generic
 * <em>resources</em>. The design emphasizes generality, safety and
 * performance.
 *
 * <ul>
 *  <li><em>Generality</em>: The available interactions consist of the
 *  conventional "CRUD": Create, Read, Update, and Delete. These have an HTTP-
 *  like flare -- set, get, update, delete -- though the types in this package
 *  do <em>not</em> depend on HTTP as an implementation technology. For an
 *  HTTP implementation, see
 *  {@link com.eharmony.matching.playground.crud.http}.</li>
 *
 *  <li><em>Safety</em>: The types of resources are statically type-safe,
 *  configured by means of generic type parameters. And because not all
 *  resources support all operations, the operations are composable, defined
 *  in separate interfaces designed to work together.</li>
 *
 *  <li><em>Performance</em>: The API is entirely asynchronous, and it
 *  emphasizes visitors over futures to discourage unnecessary blocking.
 *  (Operations do return futures, but these deliberately do not provide
 *  access to calculated results. They are intended for advanced task-
 *  coordination activities; they are not intended for data processing.)</li>
 * </ul>
 *
 * Interactions with resources depend on one more factor, which is the
 * multiplicity of child resources of a known type:
 * <ul>
 *  <li><em>Simple</em> interfaces provide access to a single resource.</li>
 *  <li><em>Map</em> interfaces provide key-value access to a set of
 *  uniquely identified resources of homogeneous type.</li>
 * </ul>
 */
package rickbw.crud;
