(ns tlbounce.core
  (:require [ircparse.core]
            [clojure.pprint :refer [pprint]])
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(defn write [conn msg]
  (doto (:out @conn)
    (.println (str msg "\r\n"))
    (.flush)))

(defn build-irc-message [m]
  "Given a map, m, build a valid IRC message."
  (let [command (:command m)
        trailing (:trailing m)]
    (str command " :" trailing "\r\n")))

(defn parse-failure [failure-object]
  {:log [{:reason :parse-failure :failure (:failure failure-object)}]})

(defn handle-message [message]
  "Takes an IRC message and returns a map of commands to execute."
  (let [parsed-or-failure (ircparse.core/message message)] 
    (if (:failure parsed-or-failure)
      (parse-failure parse-failure)
      (let [parsed parsed-or-failure]
        (cond
         (= "PING" (:command parsed)) 
         {:reply [(build-irc-message {:command "PONG" :trailing (:trailing-params parsed)})]}
         (= "PRIVMSG" (:command parsed))
         {:log [{:reason :message :message {:from (:client-nickname parsed) :channel  (apply str (:middle-params parsed)) :body (:trailing-params parsed)}}]})))))

(defn execute-logs [conn command-map]
  (let [logs (:log command-map)]
    (dorun (map println logs)))
  command-map)

(defn execute-replies [conn command-map]
  (let [replies (:reply command-map)]
    (dorun (map write (repeat conn) replies)))
  command-map)

(defn execute-commands [conn command-map]
  "Executes a map of commands."
  (->> command-map
       (execute-logs conn)
       (execute-replies conn)))

(def startup-message
  "NICK ryantm\r\nUSER bob _ _ : Ryan Mulligan\r\nJOIN #test")

(defn conn-handler [conn]
  (while (nil? (:exit @conn))
    (let [msg (.readLine (:in @conn))]
      (if (nil? msg)
        (dosync (alter conn merge {:exit true}))
        (let [msg (str msg "\r\n")]
          (do
            (pprint msg)
            (let [command-map (handle-message msg)]
              (println command-map)
              (execute-commands conn command-map))))))))

(defn connect [port]
  (let [socket (Socket. "localhost" port)
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out})]
    (doto (Thread. #(conn-handler conn)) (.start))
    conn))

(defn -main [& _]
  (-> (connect 6667)
      (write startup-message)))
