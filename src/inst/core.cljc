(ns inst.core
  (:require
   [inst.utils :as inst]
   [tick.alpha.api :as t]))

(defn time-since
  [[t t-now] & [config]]
  (let [vocabulary (merge inst/default-vocabulary
                          (:vocabulary config))
        intervals (inst/generate-intervals inst/default-intervals
                                           (:intervals config))
        inst-now (when t-now (t/instant t-now))
        seconds-from-event (inst/diff-in-seconds (t/instant t) (or inst-now (t/instant)))
        interval (inst/find-interval intervals seconds-from-event)
        time-value (inst/time-value seconds-from-event interval)
        interval-name (inst/interval-name vocabulary interval time-value)
        now? (= :now (first interval))
        order (if now?
                [:interval]
                (:order config [:time :interval :ago]))
        ago (when-not now? {:ago (get vocabulary :ago)})]
    (when (or (pos? seconds-from-event) (zero? seconds-from-event))
      (inst/format-output order
                          (:stringify? config true)
                          (merge ago
                                 {:time time-value
                                  :interval interval-name})))))

(defn time-to
  [[t t-now] & [config]]
  (let [vocabulary (merge inst/default-vocabulary
                          (:vocabulary config))
        intervals (inst/generate-intervals inst/default-intervals
                                           (:intervals config))
        inst-now (when t-now (t/instant t-now))
        seconds-from-event (inst/diff-in-seconds (t/instant t) (or inst-now (t/instant)))
        abs-seconds (Math/abs seconds-from-event)
        interval (inst/find-interval intervals abs-seconds)
        time-value (inst/time-value abs-seconds interval)
        interval-name (inst/interval-name vocabulary interval time-value)
        now? (= :now (first interval))
        order (if now?
                [:interval]
                (:order config [:in :time :interval]))
        in (when-not now? {:in (get vocabulary :in)})]
    (when (or (neg? seconds-from-event) (zero? seconds-from-event))
      (inst/format-output order
                          (:stringify? config true)
                          (merge in
                                 {:time time-value
                                  :interval interval-name})))))
