(defproject tmx2edn "0.1.1"
  :description "Convert TMX map files to EDN"
  :url "http://github.com/niamu/tmx2edn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [net.tbt-post/zlib-tiny "0.2.0"]
                 [cljsjs/pako "0.2.7-0"]
                 [hickory "0.7.1"]])
