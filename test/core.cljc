(ns test.core
  (:require
   [inst.core :as inst]
   [tick.alpha.api :as t]
   [clojure.test :refer [deftest is testing run-tests]]))

(deftest diff-in-seconds
  (testing "Event time and Now time difference in seconds"
    (is (= 20 (inst/diff-in-seconds (t/instant "2019-12-31T11:00:00Z")
                                    (t/instant "2019-12-31T11:00:20Z"))))
    (is (= 3000 (inst/diff-in-seconds (t/instant "2019-12-31T11:00:00Z")
                                      (t/instant "2019-12-31T11:50:00Z"))))
    (is (= 25200 (inst/diff-in-seconds (t/instant "2019-12-31T11:00:00Z")
                                       (t/instant "2019-12-31T18:00:00Z"))))))

(deftest time-value
  (testing "Final time number to be rendered. It's always a whole number"
    (is (= 20 (inst/time-value 1200 [:minute {:seconds 60}])))
    (is (= 1 (inst/time-value 3601 [:hour {:seconds 3600}])))
    (is (= 2 (inst/time-value 259199 [:day {:seconds 86400}])))))

(deftest interval-name
  (testing "Final interval name to be rendered"
    (is (= "minutes" (inst/interval-name {} [:minute {:limit 3600 :seconds 60}] 2)))
    (is (= "minute" (inst/interval-name {} [:minute {:limit 3600 :seconds 60}] 1)))
    (is (= "days" (inst/interval-name {} [:day {:limit 3600 :seconds 60}] 2)))
    (is (= "minuti" (inst/interval-name {:minute ["minuto" "minuti"]} [:minute {:limit 3600 :seconds 60}] 2)))
    (is (= "giorno" (inst/interval-name {:day ["giorno" "giorni"]} [:day {:limit 3600 :seconds 60}] 1)))
    (is (= "hours" (inst/interval-name {:hour ["hour" "hours"]} [:hour {:limit 3600 :seconds 60}] 3)))
    (is (= "days" (inst/interval-name {:hour ["hour" "hours"]} [:day {:limit 3600 :seconds 60}] 3)))))

(deftest adverb-preposition-map
  (testing "Final ago name to be rendered"
    (let [past? true]
      (is (= {:ago "ago"} (inst/adverb-preposition-map {} past?)))
      (is (= {:ago "fa"} (inst/adverb-preposition-map {:ago "fa"} past?)))
      (is (= {:in "fra"} (inst/adverb-preposition-map {:in "fra"} false))))))

(deftest find-interval
  (testing "Find interval based on limit value"
    (let [intervals [[:now {:limit 5 :seconds 1}]
                     [:second {:limit 60 :seconds 1}]
                     [:minute {:limit 3600 :seconds 60}]
                     [:hour {:limit 86400 :seconds 3600}]
                     [:day {:limit 604800 :seconds 86400}]
                     [:week {:limit 2629743 :seconds 604800}]
                     [:month {:limit 31556926 :seconds 2629743}]
                     [:year {:limit #?(:clj Long/MAX_VALUE
                                       :cljs js/Number.MAX_SAFE_INTEGER)
                             :seconds 31556926}]]]
      (is (= [:hour {:limit 86400 :seconds 3600}] (inst/find-interval intervals 4000)))
      (is (= [:second {:limit 60 :seconds 1}] (inst/find-interval intervals 59.9)))
      (is (= [:month {:limit 31556926 :seconds 2629743}] (inst/find-interval intervals 4000000))))))

(deftest generate-intervals
  (testing "Sorted intervals"
    (let [intervals [[:now {:limit 5 :seconds 1}]
                     [:second {:limit 60 :seconds 1}]
                     [:minute {:limit 3600 :seconds 60}]
                     [:hour {:limit 86400 :seconds 3600}]
                     [:day {:limit 604800 :seconds 86400}]
                     [:week {:limit 2629743 :seconds 604800}]
                     [:month {:limit 31556926 :seconds 2629743}]
                     [:year {:limit #?(:clj Long/MAX_VALUE
                                       :cljs js/Number.MAX_SAFE_INTEGER)
                             :seconds 31556926}]]]
      (is (= intervals (inst/generate-intervals {}))))
    (let [intervals (inst/generate-intervals {:hour {:limit 90000}})]
      (is (= [:hour {:limit 90000 :seconds 3600}] (nth intervals 3))))
    (let [intervals (inst/generate-intervals {:second {:limit 80 :seconds 2}})]
      (is (= [:second {:limit 80 :seconds 2}] (second intervals))))
    (let [intervals (inst/generate-intervals {:now {:limit 30 :seconds 1}})]
      (is (= [:now {:limit 30 :seconds 1}] (first intervals))))))

(deftest format-output
  (testing "Test different formattings"
    (is (= "12 seconds ago"
           (inst/format-output [:time :interval :ago]
                               true
                               {:time 12
                                :interval "seconds"
                                :ago "ago"})))
    (is (= {:time 12 :interval "seconds" :ago "ago"}
           (inst/format-output [:time :interval :ago]
                               false
                               {:time 12
                                :interval "seconds"
                                :ago "ago"})))
    (is (= "seconds 12 ago"
           (inst/format-output [:interval :time :ago]
                               true
                               {:time 12
                                :interval "seconds"
                                :ago "ago"})))
    (is (= "in 12 seconds"
           (inst/format-output [:in :time :interval]
                               true
                               {:time 12
                                :interval "seconds"
                                :in "in"})))
    (is (= {:time 12 :interval "secondi" :in "fra"}
           (inst/format-output [:in :time :interval]
                               false
                               {:time 12
                                :interval "secondi"
                                :in "fra"})))))

(deftest format-time
  (testing "Test top level API"
    (is (= "2 days ago" (inst/time-since [(t/instant "2019-12-22T11:00:20Z")
                                          (t/instant "2019-12-24T11:00:20Z")]
                                         nil)))
    (is (= "2 days ago" (inst/time-since [(t/instant "2019-12-22T11:00:20Z")
                                          (t/instant "2019-12-24T11:00:20Z")])))
    (is (= "2 giorni fa" (inst/time-since [(t/instant "2019-12-22T11:00:20Z")
                                           (t/instant "2019-12-24T11:00:20Z")]
                                          {:vocabulary {:day ["giorno" "giorni"]
                                                        :ago "fa"}})))
    (is (= "48 ore fa" (inst/time-since [(t/instant "2019-12-22T11:00:20Z")
                                         (t/instant "2019-12-24T11:00:20Z")]
                                        {:vocabulary {:hour ["ora" "ore"]
                                                      :ago "fa"}
                                         :intervals {:hour {:limit 1209600}
                                                     :day {:limit 2209600}}})))
    (is (= "tra 3 giorni" (inst/time-since [(t/instant "2019-12-23T11:00:20Z")
                                            (t/instant "2019-12-20T11:00:20Z")]
                                           {:vocabulary {:in "tra"
                                                         :day ["giorno" "giorni"]}})))))

;;(run-tests)
