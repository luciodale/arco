# Inst <img src="https://static.thenounproject.com/png/199376-200.png" width="30"/>

[![Clojars Project](https://img.shields.io/clojars/v/inst.svg)](https://clojars.org/inst)

A multilanguage instant formatter for displaying the time that has passed since a certain event.
In other words, the **5 minutes ago**, or **1 hour ago**, or **2 weeks ago**, or any *Time - Interval - Ago* text you see everywhere.

## Why Inst?
Because you hate writing boilerplate code. With *Inst*, you call one function and pass an instant. Done. You can move on to the next ticket.

Additionally, if you need more control over the output, you can pass a configuration map to customize the wording, the order, and the interval limits. It also works well with different languages such as Italian, French, Spanish, Japanese, ... and probably many others I have not tested yet :).


Let's look at the API.

## API

### Require Inst

#### In Deps

```clojure
inst {:mvn/version "0.1.3"}
```

or

```clojure
inst {:git/url "https://github.com/luciodale/inst.git"
:sha "last sha commit here"}
 ```

#### In Namespace

```clojure
(ns your.namespace
  (:require
   [inst.core :as inst]))
```

### The fastest way

```clojure
(inst/time-since ["2019-12-27T11:00:20Z"])
```

If you want to specify a *Now* time, you can add it as second element in the vector:

```clojure
(inst/time-since ["2019-12-27T11:00:20Z" "2019-12-29T11:00:20Z"])

=> "2 days ago"
```

Keep in mind that `#inst` dates are as well accepted.

### Customizing the language

To add a custom language, you can pass a map with a `:vocabulary` key and an optional `:order` key, which defaults to `[:time :interval :ago]`. In the following example, you can see how easy it is to add *Italian* support.

```clojure
(inst/time-since ["2019-12-27T11:00:20Z"]
                 {:vocabulary {:ago "fa"
                               :second ["secondo" "secondi"]
                               :minute ["minuto" "minuti"]
                               :hour ["ora" "ore"]
                               :day ["giorno" "giorni"]
                               :week ["settimana" "settimane"]
                               :month ["mese" "mesi"]
                               :year ["anno" "anni"]}
                  :order [:time :interval :ago]})

=> "1 giorno fa"
```

Clearly, if you want to support many languages, you can dynamically pass the `:vocabulary` and `:order` values to respect the semantics of the language in question. Each interval key takes a vector with singular and plural form, respectively.

If you are facing an instance where the language doesn't follow any of the `[:time :interval :ago]` permutations, you can add a `:stringify? false` key value pair to return a map containing each individual part of the final string.

```clojure
(inst/time-since ["2019-12-27T11:00:20Z"]
                 {:vocabulary {...}
                  :stringify? false})

=> {:time 1, :interval "giorno", :ago "fa"}
```

In this way, you have the chance to further parse the result yourself.

### Customizing the limit

Along with `:vocabulary`, `:order`, and `:stringify?` you can pass an `:intervals` key to override the default interval limits:

```clojure
(inst/time-since ["2019-12-27T11:00:00Z" "2019-12-27T11:01:30Z"]
                 {:intervals {:second {:limit 160}}})

=> "90 seconds ago"
```

For completeness, you can find the default values that can be overridden or extended in the snippet below:

```clojure
{:second {:limit 60 :seconds 1}
 :minute {:limit 3600 :seconds 60}
 :hour {:limit 86400 :seconds 3600}
 :day {:limit 604800 :seconds 86400}
 :week {:limit 2629743 :seconds 604800}
 :month {:limit 31556926 :seconds 2629743}
 :year {:limit js/Number.MAX_SAFE_INTEGER :seconds 31556926}}
```

Keep in mind that if you choose for example a `:minute` limit that goes above 86400, you will have to increase the `:hour` limit as well, as the function returns the first interval whose `:limit` value is above the event time.


### Customizing the interval

Let's add a new interval unit, being century.

```clojure
(inst/time-since ["1819-12-27T11:00:20Z"]
                 {:vocabulary {:century ["century" "centuries"]}
                  :intervals {:year {:limit 3155692600}
                              :century {:limit js/Number.MAX_SAFE_INTEGER :seconds 3155692600}}})

=> "2 centuries ago"
```

As you can see, we provide the vocabulary for the new interval, reduce the default `:year` limit, and add a new `:century` interval by setting the `:limit` and `:seconds` keys.
