(ns arco.utils
  (:require
   [tick.alpha.api :as t]
   [clojure.string :as string]))

(def default-intervals {:now {:limit 6 :seconds 1}
                        :second {:limit 60 :seconds 1}
                        :minute {:limit 3600 :seconds 60}
                        :hour {:limit 86400 :seconds 3600}
                        :day {:limit 604800 :seconds 86400}
                        :week {:limit 2629743 :seconds 604800}
                        :month {:limit 31556926 :seconds 2629743}
                        :year {:limit #?(:clj Long/MAX_VALUE
                                         :cljs js/Number.MAX_SAFE_INTEGER)
                               :seconds 31556926}})

(def default-vocabulary {:ago "ago"
                         :in "in"
                         :now "just now"
                         :second ["second" "seconds"]
                         :minute ["minute" "minutes"]
                         :hour ["hour" "hours"]
                         :day ["day" "days"]
                         :week ["week" "weeks"]
                         :month ["month" "months"]
                         :year ["year" "years"]})

(defn generate-intervals
  "To generate intervals in crescent order,
  after merging any user specific default interval."
  [default-intervals intervals]
  (->> intervals
       (merge-with merge default-intervals)
       (sort-by (comp :limit second) <)))

(defn diff-in-seconds
  "To convert time difference into raw seconds."
  [time-event time-now]
  (t/seconds (t/duration
              {:tick/beginning time-event
               :tick/end time-now})))

(defn find-interval
  [intervals diff-in-seconds]
  (some #(when (> (-> % second :limit) diff-in-seconds) %) intervals))

(defn time-value
  [diff-in-seconds interval]
  (int (Math/floor (/ diff-in-seconds (-> interval second :seconds)))))

(defn interval-name
  [vocabulary interval time-value]
  (let [time-unit (first interval)]
    (cond
      (= :now time-unit) (get vocabulary time-unit)
      (= 1 time-value) (first (get vocabulary time-unit))
      :else (second (get vocabulary time-unit)))))

(defn format-output
  [order stringify? data]
  (if stringify?
    (->> order
         (map #(get data %))
         (string/join " "))
    data))
