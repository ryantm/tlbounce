(ns tlbounce.core
  (:require [tlbounce.api :refer [api-server-start]]
            [tlbounce.irc :refer [connect write stdin-to-privmsg startup-message]]))

(defn -main [& _]
  (let [connection (connect "localhost" 6667)]
    (write connection startup-message)
    (future (stdin-to-privmsg connection))
    (api-server-start connection)))
