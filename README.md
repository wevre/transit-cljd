# transit-cljd

Transit is a data format and set of libraries for conveying values between
applications written in different languages. This library provides support for
marshalling Transit data to/from ClojureDart.

* [Transit Rationale](https://blog.cognitect.com/blog/2014/7/22/transit)
* [Transit Specification](https://github.com/cognitect/transit-format)
* [transit-cljd API docs] coming soon

This implementation's major.minor version number corresponds to the version of
the Transit specification it supports.

_NOTE: Transit is intended primarily as a wire protocol for transferring data
between applications. If storing Transit data durably, readers and writers are
expected to use the same version of Transit and you are responsible for
migrating/transforming/re-storing that data when and if the transit format
changes._

## Releases and Dependency Information

* Latest release: 0.8.16 <!--TRANSIT_CLJD_VERSION-->

As a git dep:

<!--TRANSIT_CLJD_DEPS-->
```
io.github.wevre/transit-cljd {:git/tag "v0.8.16" :git/sha "7093d15"}
```

## Usage

```
(ns main
  (:require [wevre.transit-cljd :as transit]
            ["dart:async" :as async]))

(defn main []
  (let [codec (transit/json-codec)
        objects ["foo" {:a [1 2]}]
        _ (println "Object: " objects)
        encoded (await
                 (-> (async/Stream.fromIterable objects)
                     (.transform (.-encoder codec))
                     .toList))
        _ (println "Encoded: " encoded)
        decoded (await
                 (-> (async/Stream.fromIterable encoded)
                     (. #/(cast String))
                     (.transform (.-decoder codec))
                     .toList))
        _ (println "Decoded: " decoded)]))
```

## Default Type Mapping

Coming soon.

## Testing

To run the roundtrip verification tests in `transit-format`, first ensure
Dart>=2.19.4 and Java 8 are installed, then do the following:

1. Set up a testing directory where `transit-format` and `transit-cljd` can be
   cloned side-by-side. We have a fork `transit-format` that has support for
   testing `transit-cljd` all ready to go. Clone this to your test directory.

```sh
mkdir transit-test
cd transit-test
git clone https://github.com/wevre/transit-format.git
# Do something similar to the following to ensure Java 8.
jenv local 1.8
```

2. From the `transit-format` directory, run the verify command.

```sh
bin/verify -impls cljd
```

_NOTE: The above instructions might not quite work because of incompatibilities
between Java 8 and later versions. ClojureDart has until very recently required
Java 11. You might need to manually compile the 'wevre.roundtrip' namespace in
Java 11, and then run verify with Java 8._

```sh
# Do this in the transit-test directory with Java 11.
git clone git@github.com:wevre/transit-cljd
cd transit-cljd
clj -M:cljd compile wevre.roundtrip
cd ../transit-format
bin/verify -impls cljd
```

## Copyright and License

Copyright © 2023 Michael J. Weaver

This library is a ClojureDart port of the Clojure version created and maintained
by Cognitect.

Copyright © 2014-2020 Cognitect

Licensed under the Apache License, Version 2.0 (the "License"); you may not use
this file except in compliance with the License. You may obtain a copy of the
License at https://www.apache.org/licenses/LICENSE-2.0. Unless required by
applicable law or agreed to in writing, software distributed under the License
is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the specific language
governing permissions and limitations under the License.