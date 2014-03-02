(ns omchaya.ui
  "Imperative UI functions, useful for things like updating scroll
   position or manipulating audio tags."
  (:require [cljs.core.async :as async :refer [>! <! alts! chan sliding-buffer put! close!]]
            [clojure.string :as string]
            [dommy.core :as dommy]
            [omchaya.api.mock :as api]
            [omchaya.components.app :as app]
            [omchaya.datetime :as dt]
            [omchaya.mock-data :as mock-data]
            [omchaya.useful :as useful :refer [ffilter]]
            [omchaya.utils :as utils]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [cljs.core.async.macros :as am :refer [go go-loop alt!]])
  (:use-macros [dommy.macros :only [sel sel1]]))

(defn scroll-to-latest-message! [target channel-id]
  (let [channel (sel1 target [(str "#channels-" channel-id) :.stack-panel-view])
        activities (and channel (sel channel :.activity))
        latest (last activities)
        container (when latest (.-parentElement latest))]
    (when (and channel latest)
      (set! (.-scrollTop channel) (.-offsetTop container)))))

(defn scroll-to-latest-message-when-appropriate!
  "If the second-to-last message is visible in the chat viewport, then
  scroll to the latest message"
  [target channel-id]
  (let [channel-el (sel1 target [(str "#channels-" channel-id) :.stack-panel-view])
        activities-els (sel channel-el :.activity)
        second-latest-el (last (drop-last activities-els))
        second-latest-container (when second-latest-el (.-parentElement second-latest-el))
        latest-el  (last activities-els)
        latest-container (when latest-el (.-parentElement latest-el))]
    (when (and channel-el second-latest-el)
      (let [channel-view-bottom (+ (.-scrollTop channel-el)
                                   (.-clientHeight channel-el))]
        (when (> channel-view-bottom (.-offsetTop second-latest-container))
          (set! (.-scrollTop channel-el) (.-offsetTop latest-container)))))))
