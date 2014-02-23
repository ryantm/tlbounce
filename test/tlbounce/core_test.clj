(ns tlbounce.core-test
  (:require [clojure.test :refer :all]
            [tlbounce.core :refer :all]))

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
             ["PONG :cameron.freenode.net\r\n"])))))
