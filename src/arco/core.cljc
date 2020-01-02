(ns arco.core
  (:require
   [arco.utils :as utils]
   [tick.alpha.api :as t]))

(defn time-since
  [[t t-now] & [config]]
  (let [vocabulary (merge-with #(or %2 %1)
                               utils/default-vocabulary
                               (:vocabulary config))
        intervals (utils/generate-intervals utils/default-intervals
                                           (:intervals config))
        inst-now (when t-now
                   (t/instant t-now))
        seconds-from-event (utils/diff-in-seconds (t/instant t)
                                                  (or inst-now (t/instant)))
        interval (utils/find-interval intervals
                                      seconds-from-event)
        time-value (utils/time-value
                    seconds-from-event
                    interval)
        interval-name (utils/interval-name
                       vocabulary
                       interval
                       time-value)
        now? (= :now (first interval))
        order (if now?
                [:interval]
                (:order config [:time :interval :ago]))
        ago (when-not now?
              {:ago (get vocabulary :ago)})]
    (when (or (pos? seconds-from-event)
              (zero? seconds-from-event))
      (utils/format-output order
                          (:stringify? config true)
                          (merge ago
                                 {:time time-value
                                  :interval interval-name})))))

(defn time-to
  [[t t-now] & [config]]
  (let [vocabulary (merge-with #(or %2 %1)
                               utils/default-vocabulary
                               (:vocabulary config))
        intervals (utils/generate-intervals utils/default-intervals
                                           (:intervals config))
        inst-now (when t-now
                   (t/instant t-now))
        seconds-from-event (utils/diff-in-seconds
                            (or inst-now (t/instant))
                            (t/instant t))
        interval (utils/find-interval
                  intervals
                  seconds-from-event)
        time-value (utils/time-value
                    seconds-from-event
                    interval)
        interval-name (utils/interval-name
                       vocabulary
                       interval
                       time-value)
        now? (= :now (first interval))
        order (if now?
                [:interval]
                (:order config [:in :time :interval]))
        in (when-not now?
             {:in (get vocabulary :in)})]
    (when (or (pos? seconds-from-event)
              (zero? seconds-from-event))
      (utils/format-output order
                          (:stringify? config true)
                          (merge in
                                 {:time time-value
                                  :interval interval-name})))))
