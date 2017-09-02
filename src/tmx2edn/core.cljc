(ns tmx2edn.core
  (:require [hickory.core :as hickory]
            [hickory.convert :as convert]
            [hickory.select :as s]
            [clojure.string :as string]
            [clojure.data.json :as json]))

(defn properties
  [props]
  (->> props
       (remove string?)
       (reduce (fn [accl {:keys [attrs]}]
                 (assoc accl (:name attrs) (:value attrs)))
               {})))

(defn map-data
  [file]
  (->> (slurp file)
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
           (assoc (:attrs ts)
                  :image (:source image)
                  :imagewidth (:width image)
                  :imageheight (:height image)))))

(defn tilelayer
  [l]
  (let [data (->> (s/select (s/child (s/tag :data)) l)
                  first)
        props (->> (s/select (s/child (s/tag :properties)) l)
                   first :content properties)]
    (merge {:opacity 1
            :type :tilelayer
            :visible true
            :x 0
            :y 0}
           (:attrs data)
           {:data (string/trim (first (:content data)))
            :properties props}
           (:attrs l))))

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
                          {(:tag shape) (:attrs shape)})))]
    (apply merge
           {:name ""
            :id (inc idx)
            :rotation 0
            :visible true
            :properties (->> (s/select (s/child (s/tag :properties)) o)
                             first :content properties)}
           (:attrs o)
           shapes)))

(defn objectgroup
  [og]
  (let [objects (->> (s/select (s/child (s/tag :object)) og)
                     (map-indexed object))]
    (dissoc (merge {:opacity 1
                    :type :objectgroup
                    :draworder :topdown
                    :visible true
                    :x 0
                    :y 0}
                   (:attrs og)
                   {:objects objects})
            :width
            :height)))

(defn tmx->edn
  [file]
  (let [m (map-data file)]
    (assoc (:attrs m)
           :type :map
           :properties (->> (s/select (s/child (s/tag :properties)) m)
                            first :content properties)
           :tilesets (->> (s/select (s/child (s/tag :tileset)) m)
                          (map tileset))
           :layers (apply merge
                          (->> (s/select (s/child (s/tag :layer)) m)
                               (map tilelayer))
                          (->> (s/select (s/child (s/tag :objectgroup)) m)
                               (map objectgroup))))))

(defn tmx->json
  [file]
  (json/write-str (tmx->edn file)))

(defn -main
  [file]
  (clojure.pprint/pprint (tmx->edn file)))
