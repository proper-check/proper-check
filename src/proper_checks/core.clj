(ns proper-checks.core
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]))

;;fns [fn1, fn2, fn3]
;;pos [po1, po2, po3]
;;test-suite: {
;;init fn
;;init md
;;[
;;fn1 -> fn -> fn -> fn
;;md1 -> md -> md -> md
;;ps1 -> ps -> ps -> ps
;;]
;;             }


(def counter (atom 0))
(defn incr [x] (swap! x inc) @x)
(defn decr [x] (swap! x dec) @x)
(def getc deref)

(def model-fns {incr inc
                decr dec
                getc identity})

(tc/quick-check 4
                (prop/for-all [fns (gen/not-empty (gen/vector (gen/elements [incr decr getc])))]
                              (do
                                (reset! counter 0)
                                (reduce (fn [acc fun]
                                          (if (or (false? acc) (nil? acc))
                                            false
                                            (let [act (fun counter)
                                                  exp ((get model-fns fun) acc)]
                                              (if (= act exp)
                                                exp
                                                (println (str act " != " exp))))))
                                        0 fns))))
(def suite {:init-fn (fn [] (atom 0))
            :init-md (fn [] 0)
            :actions {getc {:run (fn [x] (identity x))
                            :post (fn [real modeled] (= real modeled))}
                      incr {:run inc :post (constantly true)}
                      decr {:run dec :post (constantly true)}}})


(defn proper-check [amount suite]
  (tc/quick-check amount 
                  (let [actions (keys (:actions suite))] 
                    (prop/for-all [fns (gen/not-empty (gen/vector (gen/elements actions)))]
                                  (do
                                    (let [init-real ((:init-fn suite))
                                          init-model ((:init-md suite))]
                                      (reduce (fn [acc fun]
                                                (if (or (false? acc) (nil? acc))
                                                  false
                                                  (let [real-result (fun init-real)
                                                        model-fn (get (:actions suite) fun)
                                                        model-result ((:run model-fn) acc)]
                                                    (if ((:post model-fn) real-result model-result)
                                                      model-result
                                                      nil))))
                                              init-model
                                              fns)))))))
(proper-check 300 suite)
