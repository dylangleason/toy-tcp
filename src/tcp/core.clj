;;;;===================================================================
;;;; File     : core.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 04-26-2014
;;;; Modified : 05-30-2014
;;;;
;;;; Main program
;;;;===================================================================

(ns tcp.core
  (:require [tcp.client :as client]
            [tcp.server :as server]
            [tcp.console :as console]
            [tcp.middleware :as middleware])
  (:gen-class))


(defn get-input [prompt]
  (print prompt)
  (flush)
  (read-line))

(defn run-client
  []
  (let [host-ip   (get-input "Enter host IP: ")
        host-port (read-string (get-input "Enter host port: "))
        timeout   (read-string (get-input "Enter request pace (ms): ")) 
        requests  (read-string (get-input "Enter number of requests: "))]
    (client/run :async 1 host-ip host-port timeout requests)
    (recur)))

(defn run-server
  []
  (let [port  2605
        run   (server/run port)]
    (if @run
      (console/log {:type :server-run} port)
      (console/log {:type :server-error}))))

(defn run-middleware
  []
  (let [port      3000
        host-ip   (get-input "Enter host IP: ")
        host-port (read-string (get-input "Enter host port: "))
        run       (middleware/run port host-ip host-port)]
    (if @run
      (console/log {:type :middleware-run} port)
      (console/log {:type :server-error}))))

(defn print-usage-statement
  []
  (println "usage: java -jar tcp.jar [option]\n")
  (println "\tclient - Run TCP client\n")
  (println "\tserver - Run TCP server\n")
  (println "\tmiddle - Run TCP middleware (server/client)"))

(defn print-error
  [error]
  (do
    (println (str "ERROR: " error))
    (print-usage-statement)))

(defn -main
  [& args]
  (cond
   (< (count args) 1)        (print-error "Please specify an option.")
   (> (count args) 1)        (print-error "Too many options specified.")
   (= (first args) "client") (run-client)
   (= (first args) "server") (run-server)
   (= (first args) "middle") (run-middleware)
   :else                     (print-error "Unknown option.")))
