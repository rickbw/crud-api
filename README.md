Crud API
========

This project takes its name from the acronym Create/Read/Update/Delete. It provides a generic, foundational abstraction for accessing and manipulating state in the Java programming language. This abstraction is implemented by other projects in the `crud-*` family—for example, [Crud HTTP](https://github.com/rickbw/crud-http) contains an HTTP-based implementation.

The Crud API supports data-oriented interactions based on generic _Resources_, which encapsulate state and build upon the [Observable](https://github.com/Netflix/RxJava/blob/master/rxjava-core/src/main/java/rx/Observable.java) abstraction from [RxJava](https://github.com/Netflix/RxJava/). The design emphasizes generality, safety and concurrency.

* _Generality_: The available interactions consist of the conventional “CRUD”: Create, Read, Update, and Delete. These have an HTTP-like flare—set (i.e. PUT), get, update (i.e. POST), delete—though the types in this package do _not_ depend on HTTP as an implementation technology. These operations should also be familiar to anyone who has worked with other data-oriented APIs, such as SQL/JDBC.
* _Safety_: The types of resources are statically type-safe, configured by means of generic type parameters. And because not all resources support all operations, the operations are composable, defined in separate interfaces designed to work together. For example, a resource that supports reading and writing, but not deletion, would implement [GettableSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/GettableSpec.java) and [SettableSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/SettableSpec.java) but not [DeletableSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/DeletableSpec.java).
* _Concurrency_: The API encourages asynchronous implementations. It encapsulates asynchrony using RxJava’s `Observable` class. This encapsulation means that applications can work with asynchronous implementations just as easily as synchronous ones, and cross-cutting behaviors like retries can be transparently composed as needed.


API Overview
------------
There are two primary abstractions in the API: `Resources` and `ResourceProviders`. The former encapsulate the I/O operations on state, and hence uses a reactive style. There are four interfaces derived from [Resource](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/Resource.java), one for each CRUD operation:
* [GettableSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/GettableSpec.java)
* [SettableSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/SettableSpec.java)
* [UpdatableSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/UpdatableSpec.java)
* [DeletableSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/DeletableSpec.java)

The latter abstraction, `ResourceProvider`, provides local (i.e. assumed-cheap) navigation among `Resources`. This navigation uses a key-value lookup idiom, where keys are generic and may be simple—e.g. a URL—or arbitrarily complex—e.g. a database query—depending on the underlying data-access technology. There are four derived [ResourceProvider](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/ResourceProvider.java) interfaces:
* [GettableProviderSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/GettableProviderSpec.java)
* [SettableProviderSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/SettableProviderSpec.java)
* [UpdatableProviderSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/UpdatableProviderSpec.java)
* [DeletableProviderSpec](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/spi/DeletableProviderSpec.java)

In addition to these core abstractions, this library provides a number of
utilities of two kinds in corresponding packages:

* The `util` package contains general utilities. The related `util.rx` package contains utilities specific to working with RxJava.
* The `pattern` package contains ready-to-implement combinations of Crud interfaces intended to meet the needs of certain data-access patterns out of the box. For example, [KeyValueResource](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/pattern/KeyValueResource.java) emulates a `Map` by combining read, write and delete operations while omitting (partial) update.


See Also
--------
* The [Crud HTTP](https://github.com/rickbw/crud-http) project (`crud-http`) implements this API for HTTP, based on [Jersey](https://jersey.java.net).
* The [Crud JDBC](https://github.com/rickbw/crud-jdbc) project (`crud-jdbc`) implements this API for JDBC.
* The [Crud Voldemort](https://github.com/rickbw/crud-voldemort) project (`crud-voldemort`) implements this API for [Project Voldemort](http://www.project-voldemort.com).


Copyright and License
---------------------
All files in this project are copyright Rick Warren and, unless otherwise noted, licensed under the terms of the Apache 2 license.
