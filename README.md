# trivium [![Build Status](https://travis-ci.org/trivium-io/trivium.svg)](https://travis-ci.org/trivium-io/trivium) [![Documentation Status](https://readthedocs.org/projects/trivium/badge/?version=latest)](http://trivium.readthedocs.org/en/latest/?badge=latest)

Trivium is a platform which follows the rules of SEDA ([staged event-driven architecture](https://en.wikipedia.org/wiki/Staged_event-driven_architecture)).
So the whole information processing can be broken down into little steps and the runtime determines the best way to invoke those peaces.

## features

* free from external dependencies
* unites in-flight and at-rest information processing
* configuration free object persistence
* java-based object store query syntax

## how to start

Right now, cloning the repo and starting it within the IDE is the best way.

For every release, there are runnable shell scripts you can use, but at such an early stage it is recommended to clone the repo.

###shell script

./trivium.sh.

###jar file

java  -Djava.protocol.handler.pkgs=io.trivium.urlhandler -jar trivium.jar

## Contribute

* Issue Tracker: [https://github.com/trivium-io/trivium/issues](https://github.com/trivium-io/trivium/issues)
* Source Code: [https://github.com/trivium-io/trivium](https://github.com/trivium-io/trivium)

## Contact

If you are having issues or general feedback, please let us know.
We have a mailing list located at: trivium-io@google-groups.com

## License

The project is licensed under the Apache license.
