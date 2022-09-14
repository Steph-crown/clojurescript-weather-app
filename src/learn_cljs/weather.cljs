(ns ^:figwheel-hooks learn-cljs.weather
  (:require
   [goog.dom :as gdom]
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [ajax.core :refer [GET]]))

(defonce app-state (r/atom {:title "WhichWeather"
                            :postal-code ""
                            :temperatures {:today {:label "Today"
                                                    :value nil}
                                            :tomorrow {:label "iomorrow"
                                                      :value "ppp"}}}))
(defn title [] 
  [:h1 (:title @app-state)])

(defn temperature [temp]
  [:div {:class "temperature"}
   [:div {:class "value"} (:value temp)]
   [:h2 (:label temp)]])

(defn event-value [e] 
  (-> e .-target .-value))

(defn update-text [value]
  (swap! app-state assoc :postal-code value))

(defn handle-input [e] 
  (update-text (event-value e)))

(defn handle-response [resp]
  (let [today (get-in resp ["list" 0 "main" "temp"])
        tomorrow (get-in resp ["list" 8 "main" "temp"])]
    (swap! app-state
           update-in [:temperatures :today :value] (constantly today)
           )
    (swap! app-state
           update-in [:temperatures :tomorrow :value] (constantly tomorrow))))

(defn handle-error [err]
  ((-> js/console .log) "err" err))


(defn get-forecast! []
  (let [postal-code (:postal-code @app-state)]
    (GET "http://api.openweathermap.org/data/2.5/forecast"
              {:params {"q" postal-code
                        "appid" "2a1b20c32b726d7610d27b203ad2a251"
                        "units" "imperial"}
               :handler handle-response
               :error-handler handle-error})))


(defn postal-code []
  [:div {:class "postal-code"}
   [:h3 "Enter your postal code"]
   [:input {:type "number"
            :placeholder "Postal code"
            :value (:postal-code @app-state)
            :on-change #(handle-input %)}]
   [:p (:postal-code @app-state)]
   [:button {:on-click get-forecast!} "Go"]])



(defn app []
  [:div {:class "app"}
   [title]
   [:div {:class "temperatures"}
    (for [temp (vals (:temperatures @app-state))]
      [temperature temp])]
   [postal-code]])

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount-app-element []
  (rdom/render [app] (get-app-element)))
(mount-app-element)


(defn ^:after-load on-reload [])
  (mount-app-element) 
