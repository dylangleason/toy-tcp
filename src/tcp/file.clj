;;;;===================================================================
;;;; File     : file.clj
;;;; Author   : Dylan Gleason
;;;; Created  : 05-05-2014
;;;; Modified : 05-08-2014
;;;;
;;;; File logging methods
;;;;===================================================================

(ns tcp.file
  (:require [clojure.java.io :as io :only [file writer]])
  (:import [java.util Date]
           [java.text SimpleDateFormat]))

;;; Functions for formatting log trailer

(defn- get-date
  "Get the current date in MMDDYY format"
  []
  (let [f (SimpleDateFormat. "MMddyy")
        d (Date.)]
    (.format f d)))

(defn- get-time
  "Get the current time in HHMMSS format"
  []
  (let [f (SimpleDateFormat. "HHmmss")
        t (Date.)]
    (.format f t)))

(defn- get-trailer
  "Returns a log trailer to be appended to end of the file"
  []
  (str (get-date) "|" (get-time) "|0|0|0\r\n"))

(defn create-log-dir
  "Create the log directory if it doesn't already exist"
  [dir]
  (when-not (.exists (io/file dir))
    (.mkdir (io/file dir))))

(defn write-log-file
  "Given a vector of log entries, process each entry by writing it to
  a file"
  [scenario log]
  (let [dir   "log"
        check (create-log-dir "log")
        file  (str dir "/client.scenario" scenario ".txt")]
    (with-open [w (io/writer file)]
      (doseq [x log] (.write w (str x "\r\n")))
      (.write w (get-trailer)))))

