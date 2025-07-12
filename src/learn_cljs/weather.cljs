(ns ^:figwheel-hooks learn-cljs.weather
  (:require
    [ajax.core :as ajax]
    [goog.dom :as gdom]
    [reagent.core :as r]
    [reagent.dom :as rdom]))

(defonce app-state
  (r/atom {:title "WhichWeather"
           :postal-code ""
           :temperatures
           {:today    {:label "Today"
                       :value nil}
            :tomorrow {:label "Tomorrow"
                       :value nil}}}))

(def api-key "api-key->openweathermap.org")

(defn handle-response
  [resp]
  (let [today (get-in resp ["list" 0 "main" "temp"])
        tomorrow (get-in resp ["list" 8 "main" "temp"])]
    (swap! app-state
           update-in [:temperatures :today :value] (constantly today))
    (swap! app-state
           update-in [:temperatures :tomorrow :value] (constantly tomorrow))))

(defn get-forecast!
  []
  (let [postal-code (:postal-code @app-state)]
    (ajax/GET "http://api.openweathermap.org/data/2.5/forecast"
              {:params {"id"    postal-code
                        "appid" api-key
                        "units" "imperial"}
               :handler handle-response})))

(defn title
  []
  [:h1 (:title @app-state)])

(defn temperature
  [temp]
  [:div {:class "temperature"}
   [:div {:class "value"}
    (:value temp)]
   [:h2 (:label temp)]])

(defn go
  []
  [:button {:on-click get-forecast!} "Go"])

(defn postal-code
  []
  [:div {:class "postal-code"}
   [:h3 "Enter your postal code"]
   [:input {:type "text"
            :placeholder "Postal Code"
            :value (:postal-code @app-state)
            :on-change #(swap! app-state
                               assoc :postal-code (-> % .-target .-value))}]
   [go]])

(defn app
  []
  [:div {:class "app"}
   [title]
   [:div {:class "temperatures"}
    (for [temp (vals (:temperatures @app-state))]
      [temperature temp])]
   [postal-code]])

(defn mount-app-element
  []
  (rdom/render [app] (gdom/getElement "app")))

(mount-app-element)

(defn ^:after-load on-reload
  []
  (mount-app-element))

