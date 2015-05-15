(ns tcp.timer
  (:require [tcp.transmit :only [pending-requests]]))

(defn- all-responded?
  [coll]
  (every? true? (map :pending (vals coll))))

(defn- add-standin-response
  [coll]
  ;; 1. if :standin is false and :pending is true, and current-time -
  ;; :timestamp is greater than 3000 ms, create standin response
  ;; 2. update the object pending-request object with :standin = true
  )

(defn- add-standin-responses
  [coll]
  (loop [old coll new {}]
    (if (empty? old)
      new
      (recur (rest old) (add-standin-response (first old))))))

(defn run
  []
  (when-not (all-responded? @pending-requests)
    ;; 1. Check pending-requests for any timetamps longer than 3000 ms
    ;; 2. Create standing response for each request, if haven't created already
    (Thread/sleep 500)
    (recur)))
