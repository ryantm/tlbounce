(defproject tlbounce "0.1.0-SNAPSHOT"
  :description "An IRC bouncer that logs messages into the talklibre message format."
  :url "https://github.com/talklibre/tlbounce"
  :license {:name "CC0 1.0 Universal"
            :url "http://creativecommons.org/publicdomain/zero/1.0/legalcode"}
  :main tlbounce.core
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [ircparse "0.4.0"]
                 [ring "1.2.2"]
                 [compojure "1.1.6"]
                 [org.clojure/tools.reader "0.8.3"]])
