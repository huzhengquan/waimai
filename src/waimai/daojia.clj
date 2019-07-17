(ns waimai.daojia
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc])
  (:import [clojure.lang IPersistentMap]
           [java.net URLEncoder]))

(set! *warn-on-reflection* true)

(defn- ^{:tag IPersistentMap :static true} make-base-query-params
  [^String merchantID ^IPersistentMap options]
  {"timestamp" (quot (System/currentTimeMillis) 1000)
   "version" (get options :version 10)
   "merchantID" merchantID
   "format" (get options :format "JSON")
   })

(defn ^{:tag String :static true} make-sign
  [params ^String strParameter & {:keys [^String merchantKey]
                                  :or {^String merchantKey (System/getProperty "waimai.daojia.merchantKey")}} ]
  (let [merchant-id (get params "merchantID")
        joinstr (str
                  merchantKey
                  (clojure.string/join
                    ""
                    (sort (map #(str (first %) "=" (URLEncoder/encode (str (last %)) "UTF-8"))
                               (filter #(not-empty (str (last %)))
                                       (select-keys (assoc params "strParameter" strParameter)
                                                    ["timestamp" "version" "merchantID" "format" "strParameter"])))))
                  merchantKey)]
    (digest/md5 joinstr )))

(defn ^{:static true} request
  [^String cmd payload & {:keys [^String merchantID ^String merchantKey ^String api ^boolean debug? ^String version ^String content-type]
                          :or {^String merchantID (System/getProperty "waimai.daojia.merchantID")
                               ^String merchantKey (System/getProperty "waimai.daojia.merchantKey")
                               ^String api (or (System/getProperty "waimai.daojia.api") "https://openapi.daojia.com.cn") 
                               ^String version (System/getProperty "waimai.daojia.version")
                               ^String content-type "application/json"
                               ^boolean debug? (= (System/getProperty "waimai.debug") "true")}
                          :as opts}]
  (let [str-payload (cond
                      (not= content-type "application/json") ""
                      (string? payload) payload
                      :else (json/write-str payload))
        base-params (make-base-query-params merchantID opts)
        query-params (assoc base-params "sign" (make-sign base-params str-payload :merchantKey merchantKey))]
    (when debug?
      (println :waimai-daojia-request cmd str-payload))
    (httpc/request
      (merge
        {:method :post
         :url (str api cmd)
         :headers {"content-type" content-type}
         :query-params query-params}
        (dissoc opts :merchantID :merchantKey :api :debug? :version :format :content-type)
        (case content-type
          "multipart/form-data" {:multipart payload}
          "application/json" {:body str-payload})))))

