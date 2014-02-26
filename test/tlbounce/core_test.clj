(ns tlbounce.core-test
  (:require [clojure.test :refer :all]
            [tlbounce.core :refer :all])
  (:import (java.net ServerSocket Socket SocketException)
           (java.io PrintWriter InputStreamReader BufferedReader)))

(deftest handle-message-test
  (testing "handle-message should handle a message that cannot be parsed"
    (let [log (:log (handle-message "PING"))]
      (is (vector? log))
      (is (= (count log) 1))
      (let [log-message (first log)]
        (is (contains? log-message :reason))
        (is (contains? log-message :failure)))))
  (testing "handle-message should handle a PING message"
    (let [instructions (handle-message "PING :leguin.freenode.net\r\n")]
      (is (= (:reply instructions)
             ["PONG :leguin.freenode.net\r\n"])))
    (let [instructions (handle-message "PING :cameron.freenode.net\r\n")]
      (is (= (:reply instructions)
             ["PONG :cameron.freenode.net\r\n"]))))
  (testing "should log PRIVMSG message"
    (let [log (:log (handle-message ":blah!~ryantm@localhost.localdomain PRIVMSG #test :wow!\r\n"))
          log-message (first log)]
      (is (= log-message
             {:reason :message :message {:from "blah" :channel "#test" :body "wow!"}})))))

(deftest startup-test
  (testing "sendings start up messages"
    (let [irc-port 6668
          server-socket (new ServerSocket 6668)
          conn (connect irc-port)
          accepted-socket (.accept server-socket)
          in (BufferedReader. (InputStreamReader. (.getInputStream accepted-socket)))
          out (PrintWriter. (.getOutputStream accepted-socket))]

      (write conn startup-message)

      (is (= (.readLine in)
             "NICK ryantm"))
      (is (= (.readLine in)
             "USER bob _ _ : Ryan Mulligan"))
      (.close server-socket))))

(deftest pingpong-test
  (testing "ping and pong"
    (let [irc-port 6668
          server-socket (new ServerSocket 6668)
          conn (connect irc-port)
          accepted-socket (.accept server-socket)
          in (BufferedReader. (InputStreamReader. (.getInputStream accepted-socket)))
          out (PrintWriter. (.getOutputStream accepted-socket))]
      (.println out "PING :example.com")
      (.flush out)
      (is (= (.readLine in)
             "PONG :example.com")))))
