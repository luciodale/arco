(ns inst.core
  (:require
   [tick.alpha.api :as t]
   [clojure.string :as string]))

(defn generate-intervals
  "To generate intervals in crescent order,
  after merging any user specific default interval."
  [interval]
  (->> interval
       (merge-with
        merge
        {:second {:limit 60 :seconds 1}
         :minute {:limit 3600 :seconds 60}
         :week {:limit 2629743 :seconds 604800}
         :hour {:limit 86400 :seconds 3600}
         :day {:limit 604800 :seconds 86400}
         :month {:limit 31556926 :seconds 2629743}
         :year {:limit js/Number.MAX_SAFE_INTEGER :seconds 31556926}})
       (sort-by (comp :limit second) <)))

(defn diff-in-seconds
  "To convert time difference into raw seconds."
  [time]
  (t/seconds (t/duration
              {:tick/beginning (t/instant time)
               :tick/end (t/instant)})))

(defn find-interval
  [intervals diff-in-seconds]
  (some #(when (> (-> % second :limit) diff-in-seconds) %) intervals))

(defn time-value
  [diff-in-seconds interval]
  (Math/floor (/ diff-in-seconds (-> interval second :seconds))))

(defn interval-name
  [vocabulary interval time]
  (let [one? (= time 1)
        time-unit (if one?
                    (first interval)
                    (keyword (str (name (first interval)) "s")))]
    (or (get vocabulary time-unit)
        (name time-unit))))

(defn ago-name
  [vocabulary]
  (or (get vocabulary :ago) "ago"))

(defn format-output
  [form stringify? data]
  (->> form
       (map #(get data %))
       ((fn [ordered-data]
          (if stringify?
            (string/join " " ordered-data)
            ordered-data)))))

(defn format-time
  [t & [config]]
  (let [intervals (generate-intervals (:intervals config))
        seconds-from-event (diff-in-seconds t)
        interval (find-interval intervals seconds-from-event)
        time-value (time-value seconds-from-event interval)
        interval-name (interval-name (:vocabulary config) interval time-value)
        ago (ago-name (:vocabulary config))]
    (format-output (:form config [:time :interval :ago])
                   (:stringify? config true)
                   {:time time-value
                    :interval interval-name
                    :ago ago})))
