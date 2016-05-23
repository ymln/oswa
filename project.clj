(defproject oswa "0.1.0-SNAPSHOT"
  :description "org-struct web-application"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.8.51"]
                 [reagent "0.5.1"]
                 [re-frame "0.7.0"]
                 [cljs-http "0.1.40"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [ring/ring-devel "1.4.0"]
                 [compojure "1.5.0"]
                 [clojure-csv/clojure-csv "2.0.2"]
                 [org-struct "0.1.0-SNAPSHOT"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-figwheel "0.5.0-6"]]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :main oswa.server
  :aot :all
  :hooks [leiningen.cljsbuild]
  :jar true

  :figwheel {:css-dirs ["resources/public/css"]}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljs"]
                        :figwheel {:on-jsload "oswa.core/mount-root"
                                   :ring-handler "oswa.server/reloadable-app"}
                        :compiler {:main oswa.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :asset-path "js/compiled/out"
                                   :source-map-timestamp true}}

                       {:id "min"
                        :source-paths ["src/cljs"]
                        :compiler {:main oswa.core
                                   :output-to "resources/public/js/compiled/app.js"
                                   :optimizations :advanced
                                   :closure-defines {goog.DEBUG false}
                                   :pretty-print false}}]})
