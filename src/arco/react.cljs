(ns arco.react
  (:require
   [arco.core :as arco]
   [reagent.core :as r]
   [tick.alpha.api :as t]))

(defn inner-component
  "A reactful component that updates according to the provided refresh-rate or 1000ms"
  [t-now refresh-rate arco-fn]
  (let [millis-elapsed (r/atom 0)
        interval-instance (atom (js/setInterval #(swap! millis-elapsed + refresh-rate)
                                                refresh-rate))
        now (or t-now (t/now))]
    (r/create-class
     {:component-will-unmount
      #(js/clearInterval @interval-instance)
      :reagent-render
      (fn []
        [:<> (str (arco-fn (t/+ now (t/new-duration @millis-elapsed :millis))))])})))

(defn time-since
  [[t t-now] & [config]]
  (let [refresh-rate (or (:refresh-rate config) 1000)]
    [inner-component
     t-now
     refresh-rate
     #(arco/time-since [t %] config)]))

(defn time-to
  [[t t-now] & [config]]
  (let [refresh-rate (or (:refresh-rate config) 1000)]
    [inner-component
     t-now
     refresh-rate
     #(arco/time-to [t %] config)]))
