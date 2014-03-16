(ns tlbounce.core
  (:require [ircparse.core]
            [clojure.pprint :refer [pprint]]
            [talklibre.format :refer [map->Message]])
  (:import (talklibre.format Message)
           (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(def message-max-character-length 512)
(def output-file "output.edn")

(defn write [conn msg]
  (let [msg (str msg "\r\n")]
    (if (< message-max-character-length (count msg))
      (throw (Exception. (str "Message, " msg ", exceeds " message-max-character-length " character IRC message protocol limit."))))
    (doto (:out @conn)
      (.print msg)
      (.flush)))
  conn)

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
         {:message [{:from (:client-nickname parsed) :channel  (apply str (:middle-params parsed)) :body (:trailing-params parsed)}]}
         :else
         {:log [(str "Unhandled message: " parsed)]})))))


(defn store-message [message]
  (let [message (assoc message 
                  :id (java.util.UUID/randomUUID)
                  :instant (java.util.Date.)
                  :type "IRC"
                  :source "localhost" 
                  :content-type "text/plain")
        ircmessage (pr-str (map->Message message))]
    (spit output-file ircmessage :append true)))

(defn execute-messages [conn command-map]
  (let [messages (:message command-map)]
    (dorun (map store-message messages)))
  command-map)

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
       (execute-messages conn)
       (execute-replies conn)))

(def startup-message
  "NICK ryantm\r\nUSER bob _ _ : Ryan Mulligan\r\nJOIN #test")

(defn connection-handler [conn]
  (while (nil? (:exit @conn))
    (let [msg (.readLine (:in @conn))]
      (if (nil? msg)
        (dosync (alter conn merge {:exit true}))
        (let [msg (str msg "\r\n")]
          (do
            (let [command-map (handle-message msg)]
              (execute-commands conn command-map))))))))

(defn connect [hostname port]
  (let [socket (Socket. hostname port)
        in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        out (PrintWriter. (.getOutputStream socket))
        conn (ref {:in in :out out})]
    (doto (Thread. #(connection-handler conn)) (.start))
    conn))

(defn make-privmsg [body]
  (str "PRIVMSG #test :" body))

(defn send-privmsg [conn body]
  (doall (map #(write conn (make-privmsg (apply str %))) 
              (filter (complement empty?) (partition-all (- (- message-max-character-length 2) 15) body)))))

(defn stdin-to-privmsg [conn]
  (doseq [line (line-seq (java.io.BufferedReader. *in*))] 
    (send-privmsg conn line)))

(defn -main [& _]
  (-> (connect "localhost" 6667)
      (write startup-message)
      (stdin-to-privmsg)))
