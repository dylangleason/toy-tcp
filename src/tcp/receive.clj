;;;;===================================================================
;;;; File     : receive.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 05-10-2014
;;;; Modified : 05-25-2014
;;;;
;;;; Synchronous and asynchronous receive functions for use with both
;;;; TCP client/server
;;;;===================================================================

(ns tcp.receive)

(defn format-message
  "Convert a received byte array into a string"
  [buffer]
  (let [s (str (String. buffer))]
    (-> (clojure.string/replace s "| " (str "|" 1))
        clojure.string/trim)))

(defmulti receive
  "Receive a message over a TCP socket"
  (fn [first & more] (class first)))
        
