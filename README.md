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

* Latest release: none yet -- only tag so far is 0.8.0

Coming soon: dependency info for deps.edn.

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
        encoded (. encoded #/(cast String))
        decoded (await
                 (-> (async/Stream.fromIterable encoded)
                     (.transform (.-decoder codec))
                     .toList))
        _ (println "Decoded: " decoded)]))
```

## Default Type Mapping

Coming soon.

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
