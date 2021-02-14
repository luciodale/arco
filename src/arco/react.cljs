(ns arco.react
  (:require
   [arco.core :as arco]
   [reagent.core :as r]
   [tick.alpha.api :as t]))

(defn inner-component
  "A reactful component that updates according to the provided refresh-rate or 1000ms"
  [[_ t-now] refresh-rate arco-fn config user-component]
  (let [now-fn #(or % (t/now))
        state (r/atom {:millis 0
                       :now (now-fn t-now)})
        interval-instance (atom (js/setInterval
                                 #(swap! state update :millis + refresh-rate)
                                 refresh-rate))]
    (r/create-class
     {:component-did-update
      (fn [new-props prev-props]
        (let [[new-t new-t-now] (-> new-props .-props .-argv second)
              [prev-t] (-> prev-props second)]
          ;; to check when t changes from user
          (when (not= new-t prev-t)
            (reset! state {:millis 0
                           :now (now-fn new-t-now)}))))
      :component-will-unmount
      #(js/clearInterval @interval-instance)
      :reagent-render
      (fn [[t _]]
        (let [{:keys [millis now]} @state]
          [user-component (arco-fn [t (t/+ now (t/new-duration millis :millis))]
                                   config)]))})))

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
