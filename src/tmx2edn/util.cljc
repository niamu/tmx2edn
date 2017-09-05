(ns tmx2edn.util
  (:require #?(:clj [clojure.data.codec.base64 :as base64]
               :cljs [goog.crypt.base64 :as base64])
            #?(:clj [zlib-tiny.core :as zlib])
            [#?(:clj clojure.edn :cljs cljs.reader) :as edn]
            [clojure.string :as string]
            #?(:cljs [goog.crypt :as crypt])))

(defn clean-map
  [m]
  (reduce (fn [accl [k v]]
            (assoc accl k
                   (if-let [num (and (string? v)
                                     (re-matches #"\d+" v)
                                     (edn/read-string v))]
                     num v)))
          {} m))

(defn get-int
  [buff]
  (let [offset 0]
    (bit-or (bit-and (nth buff offset) 0xFF)
            (bit-shift-left (bit-and (nth buff (+ offset 1)) 0xFF) 8)
            (bit-shift-left (bit-and (nth buff (+ offset 2)) 0xFF) 16)
            (bit-shift-left (bit-and (nth buff (+ offset 3)) 0xFF) 24))))

(defn base64-decode
  [s]
  #?(:clj (base64/decode (.getBytes s)))
  #?(:cljs (decodeStringToByteArray s)))

#?(:clj
   (defn zlib-inflate
     [b]
     (zlib/force-byte-array (zlib/inflate b))))

#?(:clj
   (defn gunzip
     [b]
     (zlib/gunzip b)))
