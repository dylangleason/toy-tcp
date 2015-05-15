;;;;===================================================================
;;;; File     : message.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 04-26-2014
;;;; Modified : 05-20-2014
;;;;
;;;; Functions and constants used for creating TCP messages
;;;; (i.e. requests and responses)
;;;;===================================================================

(ns tcp.message
  (:require [tcp.socket :as sock]))

;;; Vars

(def max-message-size 146)

(def max-timestamp-size 10)

(def max-unsigned-bytes 256)

;;; Functions

(defn- time->String
  "Return a system time as an ASCII string"
  []
  (let [time  (str (System/currentTimeMillis)) 
        index (count time)]
    (subs time (- index max-timestamp-size) index)))

(defn- make-message
  [message]
  (let [socket (:socket message)]
    (str (time->String)                     "|"
         (:request-id     message)          "|"
         (:student-name   message)          "|"
         (:student-id     message)          "|"
         (:response-delay message)          "|"
         (:client-ip      message)          "|"
         (sock/get-local-port socket)       "|"
         (sock/get-input-descriptor socket) "|"
         (sock/get-host-address socket)     "|"
         (sock/get-host-port socket))))

(defmulti message->String
  "Convert a message into an ASCII string"
  (fn [x y] (:type x)))

(defmethod message->String :request
  [type message]
  (str "REQ"                   "|"
       (make-message  message) "|"
       (:student-data message) "|"
       (:scenario-num message)))

(defmethod message->String :response
  [type message]
  (str "RSP"                    "|"
       (make-message   message) "|"
       (:response-id   message) "|"
       (:response-type message)))
