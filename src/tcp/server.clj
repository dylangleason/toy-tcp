;;;;===================================================================
;;;; File     : server.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 04-26-2014
;;;; Modified : 05-26-2014
;;;;
;;;; TCP server handles client requests and sends a byte encoded
;;;; response in Big Endian format
;;;;===================================================================

(ns tcp.server
  (:require [clojure.pprint :as pp]
            [tcp.console :as console]            
            [tcp.receive :refer :all]
            [tcp.transmit :refer :all]
            [tcp.message :as message])
  (:import [java.net ServerSocket]
           [java.io DataInputStream DataOutputStream]))

(defn- make-response
  "Given a request, create a corresponding response to be transmitted
  to the client"
  [request]
  (let [req (clojure.string/split request #"\|+")]
    {:request-id     (nth req 2)
     :student-name   (nth req 3)
     :student-id     (nth req 4)
     :response-delay (nth req 5)
     :client-ip      (nth req 6)
     :response-id    (nth req 2)
     :response-type  1}))

(defn- enqueue
  "Enqueue a request to the front of the request queue"
  [pending request]
  (dosync
   (alter pending conj request)))

(defn- dequeue
  "Remove a request from the queue and add it processed received queue"
  [pending received request]
  (dosync
   (alter received conj request)
   (alter pending #(into [] (rest %)))))

(defn- set-finished!
  "Set the finished state"
  [finished state]
  (dosync
    (ref-set finished state)))

(defn- read-request
  "Read a single request"
  [buf socket len pending]
  (let [request (format-message buf)]
    (console/log {:type :receive} socket message/max-message-size)
    (enqueue pending request)
    (console/log {:type :response} request len)))

(defn write-message
  "Write a message over the socket"
  [writer socket pending received message]
  (let [buffer   (message->bytes message)
        length   (count buffer)]
    (console/log {:type :transmit} socket message)
    (.writeShort writer length)
    (.write writer buffer 0 length)
    (dequeue pending received message)))

(defn- write-response
  [writer socket pending received request]
  (let [response (response->String (make-response request) socket)]
    (write-message writer socket pending received response)))

(defn- read-requests
  "Read requests from the client by adding them to the requests queue"
  [reader socket pending]
  (let [len (.readByte reader)
        buf (byte-array len)]
    (loop [off 0 num 0 rec nil]
      (when (< num len)
        (let [off (.read reader buf off (- len num))]          
          (recur off (+ num off) (read-request buf socket len pending)))))))

(defmethod receive java.net.Socket
  [socket pending finished]
  (with-open [reader (DataInputStream. (.getInputStream socket))]
    (while (not @finished)
      (try (if (> (.readByte reader) -1)
             (read-requests reader socket pending)
             (set-finished! finished true))
           (catch Exception e (set-finished! finished true))))
    false))

(defmethod transmit java.net.Socket
  [socket pending received finished f]
  (with-open [writer (DataOutputStream. (.getOutputStream socket))]
    (while (not @finished)
      (when-not (empty? @pending)
        (let [request (first @pending)]
          (when-not (= request (last @received))
            (f writer socket pending received request)))))
    false))

(defn serve
  "Process all requests for the given client thread"
  [socket]
  (let [pending  (ref [])
        received (ref [])
        finished (ref false)
        receive  (future (receive socket pending finished))
        transmit (future (transmit socket pending received finished write-response))]
    (while (or @receive @transmit))
    (.close socket)))

(defn run
  "Initialize and run the server"
  [port]
  (let [running (atom true)]
    (console/log {:type :server-init})
    (future
      (with-open [server-socket (ServerSocket. port)]
        (while @running
          (let [socket (.accept server-socket)]
            (future (serve socket))))))
    running))
