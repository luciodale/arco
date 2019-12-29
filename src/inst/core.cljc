(ns inst.core
  (:require
   [tick.alpha.api :as t]
   [clojure.string :as string]))

(defn generate-intervals
  "To generate intervals in crescent order,
  after merging any user specific default interval."
  [intervals]
  (->> intervals
       (merge-with
        merge
        {:second {:limit 60 :seconds 1}
         :minute {:limit 3600 :seconds 60}
         :hour {:limit 86400 :seconds 3600}
         :day {:limit 604800 :seconds 86400}
         :week {:limit 2629743 :seconds 604800}
         :month {:limit 31556926 :seconds 2629743}
         :year {:limit #?(:clj Long/MAX_VALUE
                          :cljs js/Number.MAX_SAFE_INTEGER)
                :seconds 31556926}})
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
  (let [one? (= time-value 1)
        time-unit (first interval)]
    (if one?
      (or (first (get vocabulary time-unit))
          (name time-unit))
      (or (second (get vocabulary time-unit))
          (str (name time-unit) "s")))))

(defn ago-name
  [vocabulary]
  (or (get vocabulary :ago) "ago"))

(defn format-output
  [order stringify? data]
  (if stringify?
    (->> order
         (map #(get data %))
         (string/join " "))
    data))

(defn time-since
  ([ts]
   (time-since ts {}))
  ([[t t-now] config]
   (let [intervals (generate-intervals (:intervals config))
         inst-now (when t-now (t/instant t-now))
         seconds-from-event (diff-in-seconds (t/instant t) (or inst-now (t/instant)))
         interval (find-interval intervals seconds-from-event)
         time-value (time-value seconds-from-event interval)
         interval-name (interval-name (:vocabulary config) interval time-value)
         ago (ago-name (:vocabulary config))]
     (format-output (:order config [:time :interval :ago])
                    (:stringify? config true)
                    {:time time-value
                     :interval interval-name
                     :ago ago}))))
