(ns tmx2edn.core
  (:require [tmx2edn.util :as util]
            [hickory.core :as hickory]
            [hickory.convert :as convert]
            [hickory.select :as s]
            [clojure.string :as string]
            [#?(:clj clojure.edn :cljs cljs.reader) :as edn]
            #?(:clj [clojure.java.io :as io])))

(defn properties
  [props]
  (->> props
       (remove string?)
       (reduce (fn [accl {:keys [attrs]}]
                 (assoc accl (:name attrs) (:value attrs)))
               {})))

(defn map-data
  [file-contents]
  (->> file-contents
       hickory/parse
       hickory/as-hickory
       (s/select (s/child (s/tag :map)))
       first))

(defn tileset
  [ts]
  (let [image (->> (s/select (s/child (s/tag :img)) ts)
                   first :attrs)]
    (merge {:spacing 0
            :margin 0}
           (assoc (util/clean-map (:attrs ts))
                  :image (:source image)
                  :imagewidth (:width image)
                  :imageheight (:height image)))))

(defn data
  [d]
  (let [encoding (get-in d [:attrs :encoding])
        compression-fn (condp = (get-in d [:attrs :compression])
                         "zlib" util/zlib-inflate
                         "gzip" util/gunzip
                         (fn [d] d))
        initial-data (->> (apply str (:content d)) string/trim)]
    (condp = encoding
      "base64" (->> (util/base64-decode initial-data)
                    compression-fn
                    (into [])
                    (partition 4)
                    (map (fn [b] (util/get-int (#?(:clj byte-array
                                                  :cljs clj->js) b))))
                    vec)
      "csv" (->> (string/split initial-data #",")
                 (map (fn [gid] (edn/read-string gid)))
                 vec)
      (->> (s/select (s/child (s/tag :tile)) d)
           (map (fn [tile] (edn/read-string (get-in tile [:attrs :gid]))))
           vec))))

(defn tilelayer
  [l]
  (let [data-element (->> (s/select (s/child (s/tag :data)) l)
                          first)
        data-content (data data-element)
        props (->> (s/select (s/child (s/tag :properties)) l)
                   first :content properties)]
    (merge {:opacity 1
            :type :tilelayer
            :visible true
            :x 0
            :y 0}
           (util/clean-map (:attrs data-element))
           {:data data-content
            :properties props}
           (util/clean-map (:attrs l)))))

(defn coordinates
  [coords]
  (reduce (fn [accl coord]
            (let [[x y] (string/split coord #",")]
              (conj accl {:x x :y y})))
          []
          (string/split coords #" ")))

(defn object
  [idx o]
  (let [shapes (->> (s/select (s/child (s/or (s/tag :polygon)
                                             (s/tag :polyline))) o)
                    (map (fn [shape]
                           {(:tag shape)
                            (coordinates (-> shape :attrs :points))})))
        other (->> (s/select (s/child (s/or (s/tag :ellipse)
                                            (s/tag :text))) o)
                   (map (fn [shape]
                          {(:tag shape) (util/clean-map (:attrs shape))})))]
    (apply merge
           {:name ""
            :id (inc idx)
            :rotation 0
            :visible true
            :properties (->> (s/select (s/child (s/tag :properties)) o)
                             first :content properties)}
           (util/clean-map (:attrs o))
           shapes)))

(defn objectgroup
  [og]
  (let [objects (->> (s/select (s/child (s/tag :object)) og)
                     (map-indexed object)
                     vec)]
    (dissoc (merge {:opacity 1
                    :type :objectgroup
                    :draworder :topdown
                    :visible true
                    :x 0
                    :y 0}
                   (util/clean-map (:attrs og))
                   {:objects objects})
            :width
            :height)))

(defn tmx->edn
  [file]
  (let [m (map-data file)]
    (assoc (util/clean-map (:attrs m))
           :type :map
           :properties (->> (s/select (s/child (s/tag :properties)) m)
                            first :content properties)
           :tilesets (->> (s/select (s/child (s/tag :tileset)) m)
                          (map tileset)
                          vec)
           :layers (vec (apply merge
                               (->> (s/select (s/child (s/tag :layer)) m)
                                    (map tilelayer))
                               (->> (s/select (s/child (s/tag :objectgroup)) m)
                                    (map objectgroup)))))))

#?(:clj
   (defn assets
     [path]
     (->> (file-seq (io/file path))
          (reduce (fn [accl file]
                    (if (string/ends-with? (.getName file) ".tmx")
                      (assoc accl
                             (string/replace (.getName file) ".tmx" "")
                             (tmx->edn (slurp (.getPath file))))
                      accl))
                  {}))))
