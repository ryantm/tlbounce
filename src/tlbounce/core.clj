(ns tlbounce.core
  (:require [ircparse.core]))

(defn build-irc-message [m]
  "Given a map, m, build a valid IRC message."
  (let [command (:command m)
        trailing (:trailing m)]
    (str command " :" trailing "\r\n")))

(defn parse-failure [failure-object]
  {:log [{:reason :parse-failure :failure (:failure failure-object)}]})

(defn handle-message [message]
  (let [parsed-or-failure (ircparse.core/message message)] 
    (if (:failure parsed-or-failure)
      (parse-failure parse-failure)
      (cond
       (= "PING" (:command parsed-or-failure)) 
       {:reply [(build-irc-message {:command "PONG" :trailing (:trailing-params parsed-or-failure)})]}
       ))))
