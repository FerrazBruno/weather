(ns ^:figwheel-hooks learn-cljs.weather
  (:require
   [goog.dom :as gdom]
   [reagent.dom :as rdom]
   [reagent.core :as r]
   [ajax.core :as ajax]))

(defonce app-state (r/atom {:title "WhichWeather"
                            :postal-code ""
                            :temperatures {:today {:label "Today"
                                                   :value nil}
                                           :tomorrow {:label "Tomorrow"
                                                      :value nil}}}))

(def api-key "api_key")

(defn handle-response [resp]
  (let [today (get-in resp ["list" 0 "main" "temp"])
        tomorrow (get-in resp ["list" 8 "main" "temp"])]
    (swap! app-state
           update-in [:temperatures :today :value] (constantly today))
    (swap! app-state
           update-in [:temperatures :tomorrow :value] (constantly tomorrow))))

(defn get-forecast! []
  (let [postal-code (:postal-code @app-state)]
    (ajax/GET "http://api.openweathermap.org/data/2.5/forecast"
      {:params {"q" postal-code
                "units" "metric" ;; alternatively, use "metric"
                "appid" api-key}
       :handler handle-response})))

(defn title []
  [:h1 (:title @app-state)])

(defn temperature [temp]
  [:div {:class "temperature"}
   [:h2 (:label temp)]
   [:div {:class "value"}
    (:value temp)]])

(defn postal-code []
  [:div {:class "postal-code"}
   [:h3 "Enter your postal code"]
   [:input {:type "text"
            :placeholder "Postal Code"
            :value (:postal-code @app-state)
            :on-change #(swap! app-state assoc :postal-code (-> % .-target .-value))}]])

(defn button []
  [:div {:class "button"}
   [:button {:on-click get-forecast!} "Go"]])

(defn app []
  [:div {:class "app"}
   [title]
   [:div {:class "temperatures"}
    (for [temp (vals (:temperatures @app-state))]
      [temperature temp])]
   [postal-code]
   [button]])

(defn mount-app-element []
  (rdom/render [app] (gdom/getElement "app")))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
