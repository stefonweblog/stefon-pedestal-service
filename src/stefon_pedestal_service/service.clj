(ns stefon-pedestal-service.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.body-params :as body-params]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [ring.util.response :as ring-resp]

              jig
              [jig.web
               [app :refer (add-routes)]]
              [io.pedestal.service.interceptor :as interceptor :refer (defbefore definterceptorfn)]

              [stefon-webui-common.core :as common])
    (:import (jig Lifecycle))

    )



(defbefore root-page
  [{:keys [request system url-for] :as context}]
  (assoc context :response
         (ring-resp/redirect (url-for ::index-page))))

(defbefore hello-world [context]

  (println ">> defbefore helloworld CALLED > " context)
  (let [result (common/handle-hello-world)]

    (println ">> RESULT > " result)
    result))


;; A Jig Component
(deftype Component [config]
  Lifecycle

  (init [_ system]

    ;;(println ">> init CALLED > " system)
    (add-routes system config
                ["/" {:get root-page}
                 "/helloworld" {:get hello-world}]))

  (start [_ system]

    ;;(println ">> start CALLED > " system)
    system)

  (stop [_ system]

    ;;(println ">> stop CALLED > " system)
    system))


(defn about-page
  [request]
  (ring-resp/response (format "Clojure %s" (clojure-version))))

(defn home-page
  [request]
  (ring-resp/response "Hello World!"))

(defroutes routes
  [[["/" {:get home-page}
     ;; Set default interceptors for /about and any other paths under /
     ^:interceptors [(body-params/body-params) bootstrap/html-body]
     ["/about" {:get about-page}]]]])

;; You can use this fn or a per-request fn via io.pedestal.service.http.route/url-for
(def url-for (route/url-for-routes routes))

;; Consumed by stefon-pedestal-service.server/create-server
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
