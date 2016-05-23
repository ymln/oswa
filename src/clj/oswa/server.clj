(ns oswa.server
  (:require [clojure.java.io :as io]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.reload :refer [wrap-reload]]
            [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources]]
            [oswa.solver :as solver])
  (:gen-class))

(defroutes app
  (GET "/" _
       {:status 200
        :headers {"Content-Type" "text/html; charset=utf-8"}
        :body (io/input-stream (io/resource "public/index.html"))})
  (wrap-multipart-params
    (POST "/api" [time quality finances file]
          {:status 200
           :headers {"Content-Type" "text/json"}
           :body (pr-str (solver/solve (Integer/parseInt time)
                                       (Integer/parseInt quality)
                                       (Integer/parseInt finances)
                                       (:tempfile file)))}))
  (resources "/"))

(def reloadable-app (wrap-reload app))

(defn -main [& args]
  (run-jetty reloadable-app {:port 8080}))
