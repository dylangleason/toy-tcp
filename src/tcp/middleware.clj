;;;;===================================================================
;;;; File     : middleware.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 04-26-2014
;;;; Modified : 06-03-2014
;;;;
;;;; Middleware contains both client-server functionality. Handles
;;;; requests from clients, and forwards request to another
;;;; server. Responses from server are then sent back to original
;;;; client.
;;;;===================================================================

(ns tcp.middleware
  (:require [tcp.console :as console]
            [tcp.receive :refer :all]
            [tcp.transmit :refer :all]
            [tcp.server :as server])
  (:import [java.net Socket ServerSocket]))

(defn connect-to-client
  "Receives requests from client and forwards request from server back
  to client"
  [socket client-send server-recv]
  (let [done (ref false)
        midl (ref [])
        recv (future (receive socket client-send done)) 
        trns (future (transmit socket server-recv midl done server/write-message))]
    (while (or @recv @trns))
    (.close socket)))

(defn connect-to-server
  "Forwards requests from client to the server and receives responses
  from the server"
  [host-ip host-port client-send server-recv]
  (with-open [socket (Socket. host-ip host-port)]
    (let [done        (ref false)
          client-recv (ref [])
          transmit    (future (transmit socket client-send client-recv done server/write-message))
          receive     (future (receive socket server-recv done))]
      (while (or @receive @transmit)))))

(defn run
  "Given the ServerSocket listening port and the IP address and port
  of the server to connect to, run the middleware agent"
  [port host-ip host-port]
  (let [running (atom true)]
    (console/log {:type :middleware-init})
    (future
      (with-open [server-socket (ServerSocket. port)]
        (while @running
          (let [socket      (.accept server-socket)
                client-send (ref [])
                server-recv (ref [])]
            (future (connect-to-client socket client-send server-recv))
            (future (connect-to-server host-ip host-port client-send server-recv))))))
    running))
