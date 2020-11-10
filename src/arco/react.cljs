(ns arco.react
  (:require
   [arco.core :as arco]
   [reagent.core :as r]
   [tick.alpha.api :as t]))

(defn inner-component
  "A reactful component that updates according to the provided refresh-rate or 1000ms"
  [[t t-now] refresh-rate arco-fn config user-component]
  (let [millis-elapsed (r/atom 0)
        interval-instance (atom (js/setInterval #(swap! millis-elapsed + refresh-rate)
                                                refresh-rate))
        now (or t-now (t/now))]
    (r/create-class
     {:component-will-unmount
      #(js/clearInterval @interval-instance)
      :reagent-render
      (fn []
        [user-component (arco-fn [t (t/+ now (t/new-duration @millis-elapsed :millis))]
                                 config)])})))

(defn time-since
  [{:keys [times config]} user-component]
  (let [refresh-rate (or (:refresh-rate config) 1000)]
    [inner-component times refresh-rate arco/time-since config
     user-component]))

(defn time-to
  [{:keys [times config]} user-component]
  (let [refresh-rate (or (:refresh-rate config) 1000)]
    [inner-component times refresh-rate arco/time-to config
     user-component]))
