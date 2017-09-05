(ns tmx2edn.util-test
  (:require [tmx2edn.util :as util]
            #?(:clj [clojure.test :as t]
               :cljs [cljs.test :as t :include-macros true])))

(t/deftest clean-map
  (let [m {:a "1" :b 2 :c "c" :d "d4"}
        expected-result {:a 1 :b 2 :c "c" :d "d4"}]
    (t/is (= (util/clean-map m)
             expected-result))))

(t/deftest get-int
  (t/is (= 172 (util/get-int (byte-array [-84 0 0 0]))))
  (t/is (= 171 (util/get-int (byte-array [-85 0 0 0]))))
  (t/is (= 31 (util/get-int (byte-array [287 0 0 0])))))

(t/deftest base64-decode
  (t/is (= (String. (util/base64-decode "VE1YIHBhcnNpbmch"))
           "TMX parsing!")))
