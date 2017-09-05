# tmx2edn

A Clojure(Script) library that translates
[TMX](http://doc.mapeditor.org/en/latest/reference/tmx-map-format/)
map formatted files into EDN data structures.

Once the map is represented as EDN, you can convert it to JSON for use
with a [play-cljs](https://github.com/oakes/play-cljs) game or do
other transformations to the map within Clojure.

## Usage

The `tmx->edn` function expects a TMX file contents as a string. It
will produce an EDN hash-map of the TMX data.

```Clojure
(require '[tmx2edn.core :as tmx2edn])

(tmx2edn/tmx->edn (slurp "resources/level.tmx"))
```

There is also a convenience function called `assets` which takes a
filesystem path as an argument and will recursively find all TMX files
in that path and produce an EDN hash-map where the keys are the name
of the file (without the extension) and the values are EDN data
structures returned by `tmx->edn`.

This can be used to avoid a separate build step
with [Tiled](http://www.mapeditor.org) to convert TMX maps to JSON
data. Particularly with the use of a `defmacro` trick that will load
the map data into your ClojureScript code...

In a CLJC namespace:

```Clojure
(ns mygame.tiledmaps
  #?(:clj (:require [tmx2edn.core :as tmx2edn]))
  #?(:cljs (:require-macros [mygame.tiledmaps :refer [maps*]))))

#?(:clj (defmacro maps*
          []
          (tmx2edn/assets "resources/maps")))

(def maps (maps*))
```

## License

Copyright © 2017 Brendon Walsh

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
