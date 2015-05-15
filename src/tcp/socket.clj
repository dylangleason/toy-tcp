;;;;===================================================================
;;;; File     : socket.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 04-26-2014
;;;; Modified : 05-07-2014
;;;;
;;;; Helper functions for working with java.net.Socket objects
;;;;===================================================================

(ns tcp.socket
  (:import [java.lang.reflect.Field]
           [java.io FileInputStream FileOutputStream FileDescriptor]))

;;; Really awful hack that uses reflection to get the Socket's file
;;; descriptor, since Java doesn't expose this field in the Socket
;;; interface.

(defn- get-file-descriptor
  "Get a file descriptor field via reflection"
  []
  (doto (.getDeclaredField (-> (FileDescriptor.) type) "fd")
    (.setAccessible true)))

(defn get-input-descriptor
  "Get an input descriptor for the given Socket"
  [socket]
  (let [ptr   (get-file-descriptor) 
        input (cast FileInputStream (.getInputStream socket))
        fd    (.getFD input)]
    (.getInt ptr fd)))

(defn get-output-descriptor
  "Get an output descriptor for the given Socket"
  [socket]
  (let [ptr   (get-file-descriptor) 
        input (cast FileOutputStream (.getOutputStream socket))
        fd    (.getFD input)]
    (.getInt ptr fd)))

;;; wrappers for socket methods

(defn- reformat-address
  "Strip the leading forward slash from the IP address in the Socket
  object"
  [s]
  (clojure.string/replace s #"/" ""))

(defn get-local-address
  "Given a socket, get the local IP address of the socket"
  [socket]
  (reformat-address (.getLocalAddress socket)))

(defn get-host-address
  "Given the socket, get the host IP address of the socket"
  [socket]
  (reformat-address (.getInetAddress socket)))

(defn get-local-port
  "Given the socket, get the local port of the socket"
  [socket]
  (.getLocalPort socket))

(defn get-host-port
  "Given the socket, get the host port of the socket"
  [socket]
  (.getPort socket))
