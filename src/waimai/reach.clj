(ns waimai.reach
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc])
  (:import [clojure.lang IPersistentMap]))

(set! *warn-on-reflection* true)

(defn- ^{:tag IPersistentMap :static true} make-base-payload
  [^Long appid params]
  {"timestamp" (quot (System/currentTimeMillis) 1000)
   "appid" appid 
   "params" params})

(defn ^{:tag String :static true} make-sign
  [{:strs [appid] :as params} & {:keys [^String secret ^String version ^String tag ^String action]
                                 :or {^String secret (System/getProperty "waimai.reach.secret")
                                      ^String version "v1"}} ]
  (let [joinstr (str
                  secret
                  (clojure.string/join
                    ""
                    (sort (map (fn [[k v]] (str (name k) (json/write-str v :escape-unicode false :escape-slash false))) params)))
                  version tag action secret)]
    (-> 
      joinstr
      digest/md5
      clojure.string/upper-case)))

(defn ^{:static true} request
  [^String cmd params & {:keys [^int appid ^String secret ^String api ^boolean debug? ^String version]
                         :or {^String appid (System/getProperty "waimai.reach.appid")
                              ^String secret (System/getProperty "waimai.reach.secret")
                              ^String api (or (System/getProperty "waimai.reach.api") "https://reach.waimai.uweer.com/api") 
                              ^String version (or (System/getProperty "waimai.reach.version") "v1")
                              ^boolean debug? (= (System/getProperty "waimai.debug") "true")}
                         :as opts}]
  (let [[tag action] (clojure.string/split cmd #"/" 2)
        payload (make-base-payload appid params)
        sign-payload (assoc payload "sign" (make-sign payload :secret secret :version version :tag tag :action action))]
    (when debug?
      (println :waimai-reach-request cmd sign-payload))
    (httpc/request
      (merge
        {:method :post
         :url (str api "/" version "/" cmd)
         :headers {"content-type" "application/json"} 
         :body (json/write-str sign-payload)
         :timeout 10000}
        (select-keys opts [:timeout :user-agent :keepalive :max-redirects :follow-redirects])))))

