(ns oswa.core
    (:require [cljs.core.async :refer [<!]]
              [reagent.core :as reagent]
              [re-frame.core :as r]
              [cljs-http.client :as http])
    (:require-macros [oswa.macros :refer [defsub defhandler defcomponent with-subs]]
                     [cljs.core.async.macros :refer [go]]))

(defn dispatchv [& args]
  #(do (r/dispatch (vec (concat args [(-> % .-target .-value)])))))

(defn dispatchp [& args]
  #(do
     (r/dispatch (vec args))
     (.preventDefault %)))

(def default-db
  {:route ""
   :email ""
   :password ""
   :main-form {:time ""
               :finances ""
               :quality ""}})

(defsub :route [db]
  (:route db))

(defsub :email [db]
  (:email db))

(defsub :password [db]
  (:password db))

(defsub :token [db]
  (:token db))

(defsub :main-form [db]
  (:main-form db))

(defhandler :initialize-db [_]
  default-db)

(defhandler :route [db route]
  (assoc db :route route))

(defhandler :email [db email]
  (assoc db :email email))

(defhandler :password [db password]
  (assoc db :password password))

(defhandler :token [db token]
  (assoc db :token token))

(defhandler :time [db tm]
  (assoc-in db [:main-form :time] tm))

(defhandler :finances [db finances]
  (assoc-in db [:main-form :finances] finances))

(defhandler :quality [db quality]
  (assoc-in db [:main-form :quality] quality))

(defn api-login [email password]
  (when (and (= email "ymln@ymln.name")
             (= password "12345"))
    (r/dispatch [:token "123"])
    (r/dispatch [:route ""])))

(defhandler :login [db]
  (with-subs [email [:email]
              password [:password]]
    (api-login @email @password)
    db))

(defhandler :upload-file [db tm finances quality]
  (go (let [file (-> (.getElementById js/document "file")
                     .-files
                     (aget 0))
            response (<! (http/post "/api"
                                    {:multipart-params {:time tm
                                                        :finances finances
                                                        :quality quality
                                                        :file file}}))]
        (js/alert (pr-str response))))
  db)

(defn link [route value]
   [:a {:href "javascript:void(0)" :on-click #(do (r/dispatch [:route route]) (.preventDefault %))} value])

(defcomponent header []
  [token [:token]]
  [:header
   (concat
     [link "" "Home"]
     (if @token
       [[link "logout" "Log out"]]
       [[link "login" "Log in"]
        [link "signup" "Sign up"]]))])

(defn footer []
  [:footer])

(defn page [& content]
  [:div
   [header]
   [:div content]
   [footer]
   [:link {:rel "stylesheet"
           :href "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css"
           :integrity "sha384-1q8mTJOASx8j1Au+a5WDVnPi2lkFfwwEAa8hDDdjZlpLegxhjVME1fgjWPGmkzs7"
           :cross-origin "anonymous"}]])

(defn input [label name value on-change]
  [:div.form-group
   [:label {:for name} label]
   [:input.form-control {:name name :id name :value value :on-change on-change}]])

(defcomponent main-form []
  [f [:main-form]]
  [:form {:method "post" :on-submit (dispatchp :upload-file (:time @f) (:finances @f) (:quality @f))}
   [:input {:type "file" :name "file" :id "file"}]
   [input "Time" "time" (:time @f) (dispatchv :time)]
   [input "Finances" "finances" (:finances @f) (dispatchv :finances)]
   [input "Quality" "quality" (:quality @f) (dispatchv :quality)]
   [:button.btn.btn-default {type "submit"} "Submit"]])

(defcomponent main []
  [token [:token]]
  (if @token
    [page [main-form]]
    [page "Welcome to our system!"]))

(defn validation-errors [email password]
  [(if (empty? email)
     "Email should not be empty")
   (if (empty? password)
     "Password should not be empty"
     (if (< (count password) 5)
       "Password should be at least 5 characters long"))])

(defcomponent login []
  [email [:email]
   password [:password]]
  [page
   [:h1 "Log in"]
   (apply vector :ul.errors
          (map #(vector :li.error %)
               (filter #(not (nil? %)) (validation-errors @email @password))))
   [:form {:on-submit (dispatchp :login)}
    [:label "Email"
     [:input {:value @email :on-change (dispatchv :email)}]]
    [:label "Password"
     [:input {:value @password :on-change (dispatchv :password) :type :password}]]
    [:button {:type :submit} "Log in"]]])

(defcomponent signup []
  []
  [page "signup"])

(defn logout []
  (r/dispatch-sync [:token nil])
  (r/dispatch [:route ""])
  [:div "Logging out"])

(defn component-for-route [route]
  [(case route
     "" main
     "login" login
     "signup" signup
     "logout" logout
     main)])

(defcomponent app []
  [route [:route]]
  [component-for-route @route])

(defn mount-root []
  (reagent/render [app]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (r/dispatch-sync [:initialize-db])
  (mount-root))
