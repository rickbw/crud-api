Crud API
========

This project takes its name from the acronym Create/Read/Update/Delete. It provides a generic, foundational abstraction for accessing and manipulating state in the Java programming language. This abstraction is implemented by other projects in the `crud-*` family—for example, [Crud HTTP](https://github.com/rickbw/crud-http) contains an HTTP-based implementation.

The Crud API supports data-oriented interactions based on generic _Resources_, which encapsulate state and build upon the [Observable](https://github.com/Netflix/RxJava/blob/master/rxjava-core/src/main/java/rx/Observable.java) abstraction from [RxJava](https://github.com/Netflix/RxJava/). The design emphasizes generality, safety and concurrency.

* _Generality_: The available interactions consist of simple read and write operations.
* _Safety_: The types of resources are statically type-safe,  configured by means of generic type parameters..
* _Concurrency_: The API encourages asynchronous implementations. It encapsulates asynchrony using RxJava’s `Observable` class. This encapsulation means that applications can work with asynchronous implementations just as easily as synchronous ones, and cross-cutting behaviors like retries can be transparently composed as needed.


API Overview
------------
There are two primary abstractions in the API: `Resources` and `ResourceProviders`. The former encapsulate the I/O operations on state, and hence uses a reactive style. There are two interfaces derived from [Resource](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/core/Resource.java):
* [ReadableResource](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/core/ReadableResource.java)
* [WritableResource](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/core/WritableResource.java)

The latter abstraction, `ResourceProvider`, provides local (i.e. assumed-cheap) navigation among `Resources`. This navigation uses a key-value lookup idiom, where keys are generic and may be simple—e.g. a URL—or arbitrarily complex—e.g. a database query—depending on the underlying data-access technology. There are two derived [ResourceProvider](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/core/ResourceProvider.java) interfaces:
* [ReadableResourceProvider](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/core/ReadableResourceProvider.java)
* [WritableResourceProvider](https://github.com/rickbw/crud-api/blob/master/src/main/java/rickbw/crud/core/WritableResourceProvider.java)


See Also
--------
* The [Crud HTTP](https://github.com/rickbw/crud-http) project (`crud-http`) implements this API for HTTP, based on [Jersey](https://jersey.java.net).
* The [Crud JDBC](https://github.com/rickbw/crud-jdbc) project (`crud-jdbc`) implements this API for JDBC.
* The [Crud Voldemort](https://github.com/rickbw/crud-voldemort) project (`crud-voldemort`) implements this API for [Project Voldemort](http://www.project-voldemort.com).


Copyright and License
---------------------
All files in this project are copyright Rick Warren and, unless otherwise noted, licensed under the terms of the Apache 2 license.
