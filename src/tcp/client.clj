;;;;===================================================================
;;;; File     : client.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 04-26-2014
;;;; Modified : 05-24-2014
;;;;
;;;; TCP client that sends a byte encoded message
;;;;===================================================================

(ns tcp.client
  (:require [tcp.file :as file]
            [tcp.socket :as socket]
            [tcp.console :as console]
            [tcp.message :as message]
            [tcp.receive :refer :all]
            [tcp.transmit :refer :all])
  (:import [java.net Socket]
           [java.io DataInputStream DataOutputStream]))

;;; Vars

(def sync-log (ref []))

(def async-log (agent []))

;;; Functions

(defmulti log
  "Log all requests and responses stored in the log vector to a file"
  (fn [type scenario] (:type type)))

(defmulti reset-log
  "Reset the logging data"
  (fn [x] (:type x)))

(defmulti add-entry
  "Add an entry to the log vector"
  (fn [x y] (:type x)))

(defmethod log :sync
  [type scenario]
  (file/write-log-file scenario @sync-log))

(defmethod log :async
  [type scenario]
  (file/write-log-file scenario @async-log))

(defmethod add-entry :sync
  [type entry]
  (dosync
   (alter sync-log conj entry)))

(defmethod add-entry :async
  [type entry]
  (send async-log conj entry))

(defmethod reset-log :sync
  [type]
  (dosync
   (alter sync-log (fn [_] []))))

(defmethod reset-log :async
  [type]
  (send async-log (fn [_] [])))

(defn get-random-message
  []
  (let [messages ["Hadouken!" "Shoryuken!" "Sonic Boom!"
                  "Yoga Flame!" "Tiger Uppercut!" "Spinning Bird Kick!"]
        random   (rand-int (count messages))]
    (nth messages random)))

(defn- make-request
  [id delay scenario socket]
  {:student-name   "Ryu"
   :student-id     "19-1234"
   :request-id     id
   :response-delay delay
   :host-ip        (socket/get-host-address socket)
   :host-port      (socket/get-host-port socket)
   :student-data   (get-random-message)
   :client-ip      (socket/get-local-address socket)
   :scenario-num   scenario})

(defn- reset-logs
  "Reset the log queues"
  []
  (do
    (reset-log {:type :sync})
    (reset-log {:type :async})))

(defn- log-message
  "Log a response to the console and add it to the queue of received
  responses"
  [type response length]
  (let [res (format-message response)]
    (console/log {:type :response} res length)    
    (add-entry type res)))

(defmethod transmit clojure.lang.PersistentArrayMap
  [type writer socket request]
  (let [request (request->String request socket)
        buffer  (message->bytes request)
        length  (count buffer)]
    (console/log {:type :transmit} socket request)
    (.writeShort writer length)
    (.write writer buffer 0 length)
    (add-entry type request)))

(defmethod receive clojure.lang.PersistentArrayMap
  [type reader socket]
  (let [msb (.readByte reader)
        len (.readByte reader)
        buf (byte-array len)]
    (console/log {:type :receive} socket message/max-message-size)
    (.read reader buf 0 len)
    (log-message type buf len)))

(defn- transmit-thread
  "Transmit thread will transmit n requests to the server"
  [type writer socket scenario timeout iters]
  (dotimes [x iters]
    (let [dly (if (< x 2) 1000 0)]
      (transmit type writer socket (make-request x dly scenario socket))
      (Thread/sleep timeout))))

(defn- receive-thread
  "Receiver thread will receive n responses from server"
  [type reader socket timeout iters]
  (dotimes [x iters]
    (receive type reader socket)
    (Thread/sleep timeout)))

(defmulti send-requests
  "Given a scenario number, a socket and number of requests to send,
  process all requests"
  (fn [type scenario socket timeout iters] (:type type)))

(defmethod send-requests :sync
  [type scenario socket timeout iters]
  (with-open [reader (DataInputStream. (.getInputStream socket))
              writer (DataOutputStream. (.getOutputStream socket))]
    (dotimes [x iters]
      (transmit type writer socket (make-request x 0 scenario socket))
      (receive type reader socket)
      (Thread/sleep timeout))))

(defmethod send-requests :async
  [type scenario socket timeout iters]
  (with-open [reader (DataInputStream. (.getInputStream socket))
              writer (DataOutputStream. (.getOutputStream socket))]
    (let [s (future (transmit-thread type writer socket scenario timeout iters))
          r (future (receive-thread type reader socket timeout iters))]
      (while (or @s @r)))))

(defn- create-session
  "Given the scenario number and the number of requests to send,
  create a Socket session and send the requests"
  [type scenario host-ip host-port timeout iters]
  (with-open [socket (Socket. host-ip host-port)]
    (console/log {:type :client})
    (send-requests {:type type} scenario socket timeout iters)))

(defn run
  "Given the scenario number and a number requests to send, run the
  scenario on a separate thread"
  [type scenario host-ip host-port timeout iters]
  (let [start (atom (System/currentTimeMillis))
        end   (atom nil)]
    (reset-logs)
    (create-session type scenario host-ip host-port timeout iters)
    (log {:type type} scenario)))
