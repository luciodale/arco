(ns arco.react
  (:require
   [arco.core :as arco]
   [reagent.core :as r]
   [tick.alpha.api :as t]))

(defn inner-component
  "A reactful component that updates at each second."
  [arco-fn]
  (let [now-time (r/atom (t/now))
        interval-instance (atom (js/setInterval #(reset! now-time (t/now)) 1000))]
    (r/create-class
     {:component-will-unmount
      #(js/clearInterval @interval-instance)
      :reagent-render
      (fn []
        [:<> (arco-fn @now-time)])})))

(defn time-since
  [[t _] & [config]]
  [inner-component #(arco/time-since [t %]
                                     ;; to prevent rendering an edn map
                                     (assoc config :stringify? true))])

(defn time-to
  [[t _] & [config]]
  [inner-component #(arco/time-to [t %] (assoc config :stringify? true))])
