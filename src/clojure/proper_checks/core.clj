(ns proper-checks.core
  (:use matchure)
  (:require [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [clojure.test.check :as tc])
  (:import properchecks.CircularBuffer))

;; TODO
;; full stack errors
;; convience names for methods


(defn -put [b v] (.put b v))
(defn -get [b] (.get b))
(defn -size [b] (.size b))

(def buf-size 3)

(def commands [{:target -put
                :args [gen/int]
                :pre (fn [state call]
                       true)
                :next-state (fn [state res call] []) }
               #_{:target -get
                ;;              symbolic call
                :pre (fn [state call]
                       (not (empty? state)))
                :post (fn [state res call]
                        (= res (first state)))
                :next-state (fn [state res call]
                              (rest state))}
               #_{:target -size
                :post (fn [state res call]
                        (= (count state) res))}])

(defn add-defaults [cmds]
  (map (fn [cmd]
         (merge {:args []
                 :next-state (fn [state res] state)
                 :post (constantly true)
                 :pre (constantly true)}
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
        (loop [fns fns
               model (init-model)
               real (init-real)]
          (if (empty? fns)
            (do (cleanup!) true)
            (let [[fun & args] (first fns)
                  {:keys [next-state post pre]} (find-command commands-full fun)]
              (if (pre model args)
                (let [real-result #spy/p (apply fun real args)]
                  (if (post model (cons real args) real-result)
                    (do 
                      (recur (rest fns) (next-state model real-result) real))
                    (do 
                      (cleanup!) false)))
                (recur (rest fns) model real)))))))))

(proper-checks (fn [] []) #(CircularBuffer. buf-size) #() commands 1)
(def sw (java.io.StringWriter.))
(.printStackTrace (:result (proper-checks (fn [] '[]) #(CircularBuffer. buf-size) #() commands 1)) (java.io.PrintWriter. sw))
(println sw)
