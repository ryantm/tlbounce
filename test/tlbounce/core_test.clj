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
  (testing "should store PRIVMSG message"
    (let [messages (:message (handle-message ":blah!~ryantm@localhost.localdomain PRIVMSG #test :wow!\r\n"))
          message (first messages)]
      (is (= message
             {:from "blah" :channel "#test" :body "wow!"})))))

(deftest startup-test
  (testing "sendings start up messages"
    (let [irc-port 6668
          server-socket (new ServerSocket 6668)
          conn (connect "localhost" irc-port)
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
          conn (connect "localhost" irc-port)
          accepted-socket (.accept server-socket)
          in (BufferedReader. (InputStreamReader. (.getInputStream accepted-socket)))
          out (PrintWriter. (.getOutputStream accepted-socket))]
      (.println out "PING :example.com")
      (.flush out)
      (is (= (.readLine in)
             "PONG :example.com"))
      (.close server-socket))))

(deftest sending-privmsg-test
  (testing "privmsg sending"
    (let [irc-port 6668
          server-socket (new ServerSocket 6668)
          conn (connect "localhost" irc-port)
          accepted-socket (.accept server-socket)
          in (BufferedReader. (InputStreamReader. (.getInputStream accepted-socket)))
          out (PrintWriter. (.getOutputStream accepted-socket))]
      (send-privmsg conn "hello")
      (is (= (.readLine in)
             "PRIVMSG #test :hello"))
      (let [length-of-non-message (count "PRIVMSG #test :")
            long-string (apply str (take (- 510 length-of-non-message) (repeat "A")))
            too-long-string (str long-string "L")]
        (send-privmsg conn long-string)
        (is (= (.readLine in)
               (str "PRIVMSG #test :" long-string)))

        (is (thrown? Exception
                     (write (str "PRIVMSG #test :" too-long-string))))

        (send-privmsg conn too-long-string)
        (is (= (.readLine in)
               (str "PRIVMSG #test :" long-string)))
        (is (= (.readLine in)
               (str "PRIVMSG #test :L"))))

      (.close server-socket))))
