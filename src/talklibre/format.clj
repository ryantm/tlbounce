(ns talklibre.format)

(defrecord Message [id type source channel from content-type body instant])

;; {
;; :id #uuid "f81d4fae-7dec-11d0-a765-00a0c91e6bf6"
;; :type "IRC"
;; :source "irc.freenode.net"
;; :channel "#clojure"
;; :from "ryantm"
;; :content-type "text/plain"
;; :date #inst "1985-04-12T23:20:50.52Z"
;; :body "hello world"
;; }
