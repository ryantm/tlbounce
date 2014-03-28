(ns tlbounce.api
  (:require [ring.adapter.jetty :refer [run-jetty]]))

(defn- handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World of tlbounce"})

(defn api-server-start []
  (run-jetty handler {:port 3000}))
