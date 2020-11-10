(ns arco.core
  (:require
   [arco.utils :as utils]
   [tick.alpha.api :as t]))

(defn time-since
  [[t t-now] & [config]]
  (let [{:keys [vocabulary intervals]} (utils/extract-config config)
        inst-now (if t-now (t/instant t-now) (t/instant))
        seconds-from-event (utils/diff-in-seconds (t/instant t) inst-now)
        interval (utils/find-interval intervals seconds-from-event)
        time-value (utils/time-value seconds-from-event interval)
        interval-name (utils/interval-name vocabulary interval time-value)
        now? (= :now (first interval))
        order (if now?
                [:interval]
                (:order config [:time :interval :ago]))
        ago (when-not now? {:ago (get vocabulary :ago)})]
    (when (or (pos? seconds-from-event)
              (zero? seconds-from-event))
      (utils/format-output order
                           (:stringify? config true)
                           (merge ago
                                  {:time time-value
                                   :interval interval-name})))))

(defn time-to
  [[t t-now] & [config]]
  (let [{:keys [vocabulary intervals]} (utils/extract-config config)
        inst-now (if t-now (t/instant t-now) (t/instant))
        seconds-from-event (utils/diff-in-seconds inst-now (t/instant t))
        interval (utils/find-interval intervals seconds-from-event)
        time-value (utils/time-value seconds-from-event interval)
        interval-name (utils/interval-name vocabulary interval time-value)
        now? (= :now (first interval))
        order (if now?
                [:interval]
                (:order config [:in :time :interval]))
        in (when-not now? {:in (get vocabulary :in)})]
    (when (or (pos? seconds-from-event)
              (zero? seconds-from-event))
      (utils/format-output order
                           (:stringify? config true)
                           (merge in
                                  {:time time-value
                                   :interval interval-name})))))
