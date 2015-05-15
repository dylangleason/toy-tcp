;;;;===================================================================
;;;; File     : console.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 05-05-2014
;;;; Modified : 05-08-2014
;;;;
;;;; Console logging methods
;;;;===================================================================

(ns tcp.console
  (:require [tcp.socket :as socket]))

(defmulti log
  "Log an object to the console"
  (fn [type & more] (:type type)))

(defmethod log :server-init
  [type]
  (println "Initializing the TCP server ..."))

(defmethod log :middleware-init
  [type]
  (println "Initializing the TCP middleware ..."))

(defmethod log :server-run
  [type port]
  (println (str "Server is listening on port " port ".\n")))

(defmethod log :middleware-run
  [type port]
  (println (str "Middleware is listening on port " port ".\n")))

(defmethod log :server-error
  [type]
  (println "ERROR: server socket was closed\n."))

(defmethod log :client
  [type]
  (println "Initializing the TCP client ...\n"))

(defmethod log :transmit
  [type socket message]
  (let [size (count message)]
    (println (str "Transmitting message\n"
                  "--------------------------------\n"
                  "Message Size : " size " bytes\n"
                  "Message Text : " message "\n"
                  "Host IP      : " (socket/get-host-address socket) "\n"
                  "Host Port    : " (socket/get-host-port socket) "\n"))))

(defmethod log :receive
  [type socket count]
  (println (str "Receiving message\n"
                "--------------------------------\n"
                "Maximum Size : " count " bytes\n"
                "Host IP      : " (socket/get-host-address socket) "\n"
                "Host Port    : " (socket/get-host-port socket))))

(defmethod log :response
  [type response count]
  (println (str "Bytes In     : " count))
  (println (str "Message      : " response "\n")))
