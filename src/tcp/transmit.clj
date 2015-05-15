;;;;===================================================================
;;;; File     : transmit.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 05-10-2014
;;;; Modified : 05-25-2014
;;;;
;;;; Synchronous and asynchronous send methods will send a request to
;;;; the server, perform logging and other critical operations.
;;;;===================================================================

(ns tcp.transmit
  (:require [tcp.message :as message :refer [message->String]]))

(defn request->String
  "Given a request and a socket, return the request in a string format"
  [request socket]
  (message->String {:type :request} (into {:socket socket} request)))

(defn response->String
  "Given a response and a socket, return the response in a string format"
  [response socket]
  (message->String {:type :response} (into {:socket socket} response)))

(defn message->bytes
  "Given a request, return the request in a byte array format"
  [request]
  (.getBytes request "US-ASCII"))

(defmulti transmit
  "Transmit a message over a TCP socket"
  (fn [first & more] (class first)))
