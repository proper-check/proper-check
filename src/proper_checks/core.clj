(ns proper.checks
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc]))

{:init-fn (fn [x] real-system)
 :init-md (fn [x] model-system)
 :actions {fn1 {:run (fn [x] x)
                :post (fn [x] (or false true))}
           fn2 ...
           fn3 ...}}

fns [fn1, fn2, fn3]
pos [po1, po2, po3]

test-suite: {
init fn
init md
[
fn1 -> fn -> fn -> fn
md1 -> md -> md -> md
ps1 -> ps -> ps -> ps
]
             }

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
