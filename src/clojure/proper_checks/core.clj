(ns proper-checks.core
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc])
  (:import properchecks.CircularBuffer))

(defn -put [b v] (.put b v))
(defn -get [b] (.get b))
(defn -size [b] (.size b))

(def buf-size 3)


(def commands [{:target -put
                :args [gen/int]
                :next-state (fn [state res]
                              (if (< state buf-size)
                                (inc state)
                                buf-size))}
               {:target -get
                :next-state (fn [state res]
                              (if (> state 0)
                                (dec state)
                                0))}
               {:target -size
                :post (fn [state [buf] res]
                        (= state res))}])

(defn add-defaults [cmds]
  (map (fn [cmd]
         (merge {:args []
                 :next-state (fn [state res] state)
                 :post (constantly true)}
                cmd))
       cmds))

(defn generate-calls [cmds]
  (gen/not-empty
    (gen/vector
      (gen/one-of
        (map (fn [{:keys [args target]}]
               (if (empty? args)
                 (gen/tuple (gen/return target))
                 (apply gen/tuple (gen/return target) args)))
             cmds)))))

(defn find-command [cmds cmd]
  (some (fn [{target :target :as full-cmd}]
          (when (= target cmd)
            full-cmd))
        cmds))

(defn proper-checks [init-model init-real cleanup! commands runs]
  (let [commands-full (add-defaults commands)]
    (tc/quick-check runs
      (prop/for-all [fns (generate-calls commands-full)]
                    (println "-------------")
        (loop [fns fns
               model (init-model)
               real (init-real)]
          (let [[fun & args] (first fns)
                {:keys [next-state post]} (find-command commands-full fun)
                real-result (apply fun real args)
                a (println next-state)
                new-model-state (next-state model real-result)
                post-result (post new-model-state 
                                  (cons real args)
                                  real-result)]
            (cond
              (false? post-result) (do (cleanup!) false)
              (empty? (rest fns)) (do (cleanup!) true)
              :else (recur (rest fns) new-model-state real))))))))

(proper-checks (fn [] 0) #(CircularBuffer. buf-size) #() commands 1)
(def sw (java.io.StringWriter.))
(.printStackTrace (:result (proper-checks (fn [] 0) #(CircularBuffer. buf-size) #() commands 1)) (java.io.PrintWriter. sw))
(println sw)
