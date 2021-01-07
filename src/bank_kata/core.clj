(ns bank-kata.core
  (:require
    [clojure.string :as string]
    [tick.alpha.api :as tick]
    [tick.format :as tick-format]))

(defn get-balance
  [account]
  (if (empty? account)
    0
    (:balance (last account))))

(defn get-today
  []
  (tick/date))

(defn deposit
  ([amount] (deposit [] amount))
  ([account amount]
   (conj
     account
     {:balance (+ amount (get-balance account))
      :amount amount
      :date (get-today)})))

(defn withdraw
  ([amount] (withdraw [] amount))
  ([account amount]
   (conj
     account
     {:balance (- (get-balance account) amount)
      :amount (- amount)
      :date (get-today)})))

(defn print-transactions
  [account]
  (string/join "\n"
               (map
                 #(str
                    (tick/format (tick-format/formatter "dd/MM/yyyy") (:date %))
                    " || "
                    (:amount %)
                    " || "
                    (:balance %))
                 (reverse (sort-by :date account)))))

(defn print-header
  []
  "Date || Amount || Balance\n")

(defn output-writer
  [output]
  (println output))

