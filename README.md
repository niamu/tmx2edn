# tmx2edn

A Clojure(Script) library that translates
[TMX](http://doc.mapeditor.org/en/latest/reference/tmx-map-format/)
map formatted files into EDN data structures.

Once the map is represented as EDN, you can convert it to JSON for use
with a [play-cljs](https://github.com/oakes/play-cljs) game or do
other transformations within the map.

## Usage

```Clojure
(require '[tmx2edn.core :as tmx2edn])

(tmx2edn/tmx->edn "resources/level.tmx")
```

You can also translate a map directly to JSON for convenience. This is
particularly useful for avoiding an extra build step when compiling
your game that targets a browser.

```Clojure
(tmx2edn/tmx->json "resources/level.tmx")
```

## License

Copyright © 2017 Brendon Walsh

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
