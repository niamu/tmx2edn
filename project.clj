(defproject tmx2edn "0.1.0-SNAPSHOT"
  :description "Convert TMX map files to EDN"
  :url "http://github.com/niamu/tmx2edn"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [hickory "0.7.1"]]
  :main tmx2edn.core)
