(ns oswa.core
    (:require [reagent.core :as reagent]
              [re-frame.core :as r])
    (:require-macros [oswa.core :refer [defsub defhandler defcomponent with-subs]]))

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
   :projects []
   :adding-project ""})

(defsub :route [db]
  (:route db))

(defsub :email [db]
  (:email db))

(defsub :password [db]
  (:password db))

(defsub :token [db]
  (:token db))

(defsub :adding-project [db]
  (:adding-project db))

(defsub :projects [db]
  (:projects db))

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

(defhandler :adding-project [db current-project]
  (assoc db :adding-project current-project))

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

(defhandler :add-project [db project]
  (if (and project
           (not (empty? project)))
    (update-in db [:projects] conj project)))

(defn link [route value]
   [:a {:href "javascript:void(0)" :on-click #(do (r/dispatch [:route route]) (.preventDefault %))} value])

(defn header []
  [:header
   [link "" "Home"]
   [link "login" "Log in"]
   [link "signup" "Sign up"]
   [link "logout" "Log out"]])

(defn footer []
  [:footer])

(defn page [& content]
  [:div
   [header]
   [:div content]
   [footer]])

(defn project [name]
  [:div name])

(defcomponent projects []
  [prjs [:projects]]
  [:div
   [:div (for [prj @prjs]
           [project prj])]
   [:div "Total projects: " (count @prjs)]])

(defcomponent add-project-button []
  [current-project [:adding-project]]
  [:div
   [:input {:on-change (dispatchv :adding-project) :value @current-project}]
   [:button {:on-click (dispatchp :add-project @current-project)} "Add project"]])

(defn project-list []
  [:div
   [projects]
   [add-project-button]])

(defcomponent main []
  [token [:token]]
  (if @token
    [page [project-list]]
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
