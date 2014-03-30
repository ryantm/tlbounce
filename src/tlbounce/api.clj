(ns tlbounce.api
  (:require [compojure.core :refer [defroutes GET POST]]
            [ring.adapter.jetty :refer [run-jetty]]
            [tlbounce.irc :refer [send-privmsg]]))

(defroutes handler
  (GET "/" [:as request] {:status 200
                          :headers {"Content-Type" "text/html"}
                          :body (str "Hello World of tlbounce" (:connection request))})
  (GET "/:message" [message :as request]
       (send-privmsg (:connection request) message)
       {:status 200
        :headers {"Content-Type" "text/html"}
        :body (str "Wrote " message)}))

(defn- wrap-connection [handler connection]
  (fn [request]
    (let [request (assoc request :connection connection)]
      (handler request))))

(defn api-server-start [connection]
  (-> handler
      (wrap-connection connection)
      (run-jetty {:port 3000})))
