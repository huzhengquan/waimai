(ns waimai.reach
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc])
  (:import [clojure.lang IPersistentMap]))

(set! *warn-on-reflection* true)

(defn- ^{:tag IPersistentMap :static true} make-payload
  [appid client params]
  (json/write-str
    {"timestamp" (quot (System/currentTimeMillis) 1000)
     "appid" appid 
     "client" client
     "params" params}))

(defn ^{:tag String :static true} make-sign
  [payload & {:keys [^String secret ^String version ^String cmd]
              :or {^String secret (System/getProperty "waimai.reach.secret")
                   ^String version "v1"}} ]
  (->
    (str secret "/" version "/" cmd payload secret)
    digest/md5
    clojure.string/upper-case))

(defn ^{:static true} request
  [^String cmd params & {:keys [appid client ^String secret ^String api ^boolean debug? ^String version ]
                         :or {appid (System/getProperty "waimai.reach.appid")
                              ^String secret (System/getProperty "waimai.reach.secret")
                              ^String api (or (System/getProperty "waimai.reach.api") "https://reach.waimai.uweer.com/api") 
                              ^String version (or (System/getProperty "waimai.reach.version") "v1")
                              ^boolean debug? (= (System/getProperty "waimai.debug") "true")
                              client (or (System/getProperty "waimai.reach.client") 0)}
                         :as opts}]
  (let [payload (make-payload appid client params)
        sign (make-sign payload :secret secret :version version :cmd cmd)]
    (when debug?
      (println :waimai-reach-request cmd sign))
    (httpc/request
      (merge
        {:method :post
         :url (str api "/" version "/" cmd)
         :query-params {:sign sign}
         :headers {"content-type" "application/json"} 
         :body payload
         :timeout 10000}
        (select-keys opts [:timeout :user-agent :keepalive :max-redirects :follow-redirects])))))

