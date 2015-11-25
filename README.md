# toy-tcp

A Clojure TCP app that can be configured as a client, server or middleware.

### Requirements ###

In order to build toy-tcp, you must install (at a minimum) Java SDK 7. To verify your version of Java:

```
$ java -version
```

Once Java is installed, the easiest way to build the project and get going is with [Leiningen](https://github.com/technomancy/leiningen), a build automation and dependency management tool for Clojure projects.

## Usage

To run toy-tcp from the command line, do the following:

```
$ cd ~/path/to/toy-tcp && lein run -m tcp.core [argument]
```

Alternatively, you can package cadence as a JAR file and execute it that way:

```
$ cd ~/path/to/toy-tcp && lein uberjar
$ java -jar target/tcp-0.1.0-SNAPSHOT-standalone.jar [argument]
```

One of the following command-line arguments must be specified to initialize a TCP session:

```
usage: java -jar tcp.jar [option]

	client - Run TCP client

	server - Run TCP server

	middle - Run TCP middleware (server/client)
```


## License

Copyright © 2014 Dylan Gleason

Distributed under the Eclipse Public License, the same as Clojure.
