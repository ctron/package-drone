Package Drone
=======

A package manager repository for Java (any maybe more)

This project was started to scratch the itch that Maven Tycho can compile OSGi bundles with Eclipse, but not deploy it "somewhere". Everything has to somehow copied around with B3, P2Director or other tools.

The idea is to have a workflow of Tycho Compile -> publish to repo -> Tycho Compile (using deployed artifacts). And some repository tools like cleanup, freezing, validation.

Also if you start a thing like this, it should somehow be extensible, since the next thing I would like to see it creating APT and YUM repositories.

Warning: This project is as its early stages. Try it out, find it useful or wait until it is stable enough to work for you.

What it currently can do
----------------

* Maven Tycho can deploy bundles and features (NOT repositories)
* Eclipse P2 can fetch bundles and features
* Store artifacts and metadata in a MariaDB (possible MySQL) database

Known bugs and limitation
----------------

* The concept of P2 categories and binaries is missing
* Channels only have cryptic UUID "names", they should have an alias
* Automatic cleanup is missing
* There is deadlock in Equinox/EclipseLink when updating the JPA database settings
* There is no security AT ALL
* PostgreSQL is not supported, since PostgreSQL does not support Blobs properly
* At the moment the tycho generated metadata is used for features and bundles. This requires bundles and features to be deployed to the repository using Maven Tycho including the attached "p2metadata" XML file.

What it currently cannot do
----------------

A lot of things. If there is time it will be implemented. Or you can help by contributing.

Hopefully some time package drone can:

* Provide access to bundles by an OBR layout
* Provide access to bundles by an OSGi R5 repository layout
* Act as a fully functional Maven M2 repository
