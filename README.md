# Arco <img src="https://static.thenounproject.com/png/199376-200.png" width="30"/>

[![Clojars Project](https://img.shields.io/clojars/v/arco.svg)](https://clojars.org/arco)

A multi language instant formatter to calculate the difference between the *NOW* time and past or future events. Some examples are: **5 minutes ago**, **2 hours ago**, **in 2 weeks**, **just now**.

## Why Arco? (Arc of time in Italian)
Most JS libraries already provide you with easy to use "time-ago" APIs. However, with *Arco*, you have much more granular control over your formatted output.

In fact, you can pass a configuration map to customize the wording, the final strings order, and the intervals limits. *Arco* also works well with different languages such as Italian, French, Spanish, Japanese,... and probably many others I have not tested yet :).

Also, this library is side effect free, which makes it 100% testable, and works both on your server and browser. Let's look at the API.

## API

### Require Arco & Tick

#### In Deps

```clojure
arco {:mvn/version "0.3.3"}
tick {:mvn/version "0.4.27-alpha"}
```
#### In Namespace

```clojure
(ns your.namespace
  (:require
   [arco.core :as arco]))
```

### The fastest way

```clojure
(arco/time-since ["2019-12-27T11:00:20Z"])

(arco/time-to ["2019-12-28T11:00:20Z"])
```

If you want to specify a *NOW* time (suitable for unit testing), you can add it as second element in the vectors:

```clojure
;; Past events

(arco/time-since ["2019-12-27T11:00:20Z" "2019-12-29T11:00:20Z"])
=> "2 days ago"

(arco/time-since ["2019-12-29T11:00:20Z" "2019-12-27T11:00:20Z"])
=> nil

;; Future events

(arco/time-to ["2019-12-29T11:00:20Z" "2019-12-27T11:00:20Z"])
=> "in 2 days"

(arco/time-to ["2019-12-27T11:00:20Z" "2019-12-29T11:00:20Z"])
=> nil
```

Keep in mind that `#inst` values are as well accepted.

### Customizing the language

To add a custom language, you can pass a map with a `:vocabulary` key and an optional `:order` key, which defaults to `[:time :interval :ago]` for `time-since` and `[:in :time :interval]` for `time-to`. In the following example, you can see how easy it is to add *Italian* support.

```clojure
(arco/time-since ["2019-12-27T11:00:20Z" "2019-12-28T11:00:20Z"]
                 {:vocabulary {:ago "fa"
                               :now "ora"
              ;; singular  plural
                               :second ["secondo" "secondi"]
                               :minute ["minuto" "minuti"]
                               :hour ["ora" "ore"]
                               :day ["giorno" "giorni"]
                               :week ["settimana" "settimane"]
                               :month ["mese" "mesi"]
                               :year ["anno" "anni"]}
                  :order [:time :interval :ago]})
=> "1 giorno fa"

(arco/time-to ["2019-12-28T11:00:20Z" "2019-12-27T11:00:20Z"]
              {:vocabulary {:in "tra"
                            :now "ora"
                            :second ["secondo" "secondi"]
                            :minute ["minuto" "minuti"]
                            :hour ["ora" "ore"]
                            :day ["giorno" "giorni"]
                            :week ["settimana" "settimane"]
                            :month ["mese" "mesi"]
                            :year ["anno" "anni"]}
               :order [:in :time :interval]})
=> "tra 1 giorno"
```

If you need to support many languages, you can dynamically pass the `:vocabulary` map and `:order` sequence to respect the semantics of the language in question. Also, notice that each interval key takes a vector of two elements: singular and plural form.

In the case you prefer to return the individual elements of the output separately, you can include a `:stringify? false` key value pair to the configuration map.

```clojure
(arco/time-since ["2019-12-27T11:00:20Z" "2019-12-28T11:00:20Z"]
                 {:stringify? false})
=> {:time 1, :interval "day", :ago "ago"}

(arco/time-to [ "2019-12-28T11:00:20Z" "2019-12-27T11:00:20Z"]
              {:stringify? false})
=> {:time 1, :interval "day", :in "in"}
```

In this way, you have the chance to further parse the result yourself.

### Customizing the limit

Along with `:vocabulary`, `:order`, and `:stringify?` you can pass an `:intervals` key to override the default interval units:

```clojure
(arco/time-since ["2019-12-27T11:00:00Z" "2019-12-27T11:01:30Z"]
                 {:intervals {:second {:limit 160}}})
=> "90 seconds ago"
```

For completeness, you can find the default values that can be overridden or extended in the snippet below:

```clojure
{:now {:limit 6 :seconds 1}
 :second {:limit 60 :seconds 1}
 :minute {:limit 3600 :seconds 60}
 :hour {:limit 86400 :seconds 3600}
 :day {:limit 604800 :seconds 86400}
 :week {:limit 2629743 :seconds 604800}
 :month {:limit 31556926 :seconds 2629743}
 :year {:limit #?(:clj Long/MAX_VALUE
                  :cljs js/Number.MAX_SAFE_INTEGER)
        :seconds 31556926}}
```

Keep in mind that if you choose for example a `:minute` limit that goes above 86400, you will have to increase the `:hour` limit as well, as the function returns the first interval whose `:limit` value is above the event time.

### Customizing the interval

Let's add a new interval unit, being `:century`.

```clojure
(arco/time-since ["2019-12-27T11:00:20Z" "2219-12-27T22:00:00Z"]
                 {:vocabulary {:century ["century" "centuries"]}
                  :intervals {:year {:limit 3155692600}
                              :century {:limit js/Number.MAX_SAFE_INTEGER :seconds 3155692600}}})
=> "2 centuries ago"

(arco/time-to ["2219-12-27T22:00:00Z" "2019-12-27T11:00:20Z"]
              {:vocabulary {:century ["century" "centuries"]}
               :intervals {:year {:limit 3155692600}
                           :century {:limit js/Number.MAX_SAFE_INTEGER :seconds 3155692600}}})
=> "in 2 centuries"
```

As you can see, we provide the vocabulary for the new interval, reduce the default `:year` limit, and add a new `:century` interval by setting the `:limit` and `:seconds` keys.

If you want to stop at a specific interval i.e. 385 seconds, you can pass a `:stop-at-interval :second` key value pair in the config to remove the uneeded intervals.

### Different Timezone?

No worries! Just format the instant value before passing it to the function.

```clojure
(arco/time-since ["2019-12-29T11:00:00+01:00" "2019-12-29T11:00:00"])
=> "1 hour ago"

(arco/time-to ["2019-12-29T11:00:00" "2019-12-29T11:00:00+01:00"])
=> "in 1 hour"
```

### React component

There is an `arco.react` namespace that provides a reagent component to help with live time updates. An example on how to use it follows:

```clojure
(ns your-ns
  (:require [arco.react :as ar]))

(defn main-panel
  []
  [:div
   [:span "Live time passed: "]
   [ar/time-since {:times ["2020-05-31T00:18:38.112Z"]
                   :config {:refresh-rate 2000
                            :vocabulary ...}}
    (fn [formatted-t]
      [:div "my formatted-t: " formatted-t])]])
```

Note that `ar/time-to` and `ar/time-sice` accept a `:refresh-rate` key, which indicates the rate at which the react component is re-rendered expressed in milliseconds.
