(ns bank-kata.core-test
  (:require
    [bank-kata.core :refer :all]
    [bond.james :as bond :refer [with-spy with-stub!]]
    [clojure.test :refer :all]
    [tick.alpha.api :as tick]))

(defn get-first-call-args
  [fn]
  (-> (bond/calls fn)
      first
      :args
      vec))

(deftest happy-path
  (testing "can output full statement as expected"
    (with-spy [output-writer]
      (with-stub! [[get-today [(fn [] (tick/date "2012-01-10"))
                               (fn [] (tick/date "2012-01-13"))
                               (fn [] (tick/date "2012-01-14"))]]
                   [output-writer (fn [_])]]
        (let [expected-input ["Date || Amount || Balance\n14/01/2012 || -500 || 2500\n13/01/2012 || 2000 || 3000\n10/01/2012 || 1000 || 1000"]
              account (-> (deposit 1000)
                          (deposit 2000)
                          (withdraw 500))
              _ (output-writer (str (print-header) (print-transactions account)))
              writer-input (get-first-call-args output-writer)]

          (is (= writer-input expected-input)))))))

(deftest prints-header
  (testing "can output a header formatted as expected"
    (is (= (print-header) "Date || Amount || Balance\n"))))

(deftest can-deposit
  (testing "can deposit money into an account"
    (with-stub! [[get-today (fn [] "14/01/2012")]]
      (let [account (deposit 1000)
            first-transaction (first account)
            balance (get-balance account)]

        (is (= balance 1000))
        (is (= (:amount first-transaction) 1000))
        (is (= (:date first-transaction) "14/01/2012"))))))

(deftest can-withdraw
  (testing "can withdraw money from an account"
    (with-stub! [[get-today (fn [] "13/01/2012")]]
      (let [account (withdraw 1000)
            first-transaction (first account)
            balance (get-balance account)]

        (is (= balance -1000))
        (is (= (:amount first-transaction) -1000))
        (is (= (:date first-transaction) "13/01/2012"))))))

(deftest sorts-transactions)

(deftest can-print-transactions
  (testing "can print transactions"
    (testing "according to formatting"
      (let [account [{:balance 1000, :amount 1000, :date (tick/date "2012-01-10")}]
            output (print-transactions account)
            expected "10/01/2012 || 1000 || 1000"]

        (is (= output expected))))
    (testing "and sorts by date descending"
      (let [account [{:balance 1000, :amount 1000, :date (tick/date "2012-01-10")}
                     {:balance 3000, :amount 2000, :date (tick/date "2012-01-13")}
                     {:balance 3000, :amount 2000, :date (tick/date "2012-04-13")}
                     {:balance 3000, :amount 2000, :date (tick/date "2013-01-13")}
                     {:balance 2500, :amount -500, :date (tick/date "2012-01-14")}]
            output (print-transactions account)
            expected "13/01/2013 || 2000 || 3000\n13/04/2012 || 2000 || 3000\n14/01/2012 || -500 || 2500\n13/01/2012 || 2000 || 3000\n10/01/2012 || 1000 || 1000"]

        (is (= output expected))))))
