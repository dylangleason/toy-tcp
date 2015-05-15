(defproject tcp "0.1.0"
  :description "TCP client for CST 415"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.trace "0.7.8"]]
  :profiles {:uberjar {:aot [tcp.core]}}
  :main tcp.core)
