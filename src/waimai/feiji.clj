(ns waimai.feiji
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc])
  (:import [java.net URLEncoder]
           java.util.Base64))

(set! *warn-on-reflection* true)

(defn ^{:tag String :static true} make-sign
  [^String data & {:keys [^String appid ^String secret] 
                   :or {appid (System/getProperty "waimai.feiji.appid")
                        secret (System/getProperty "waimai.feiji.secret")}} ]
  (let [md5-str ^String (digest/md5 (str data secret))]
    (.encodeToString
      ^java.util.Base64$Encoder (java.util.Base64/getEncoder) 
      ^bytes (.getBytes md5-str "UTF-8"))))

(defn ^{:static true} request
  [^String cmd params & {:keys [^String appid ^String secret ^String url ^boolean sign? ^boolean debug? ^boolean escape-unicode?]
                         :or {^String appid (System/getProperty "waimai.feiji.appid")
                              ^String secret (System/getProperty "waimai.feiji.secret")
                              ^String url (or (System/getProperty "waimai.feiji.url") "http://store.feiji-zlsd.com/feiji/")
                              ^boolean sign? true 
                              ^boolean debug? false
                              ^boolean escape-unicode? true}
                         :as opts}]
  (let [payload (if sign?
                  (let [params-str (if (string? params)
                                     params
                                     (json/write-str params :escape-unicode escape-unicode?))]
                    {:appid appid
                     :sign (make-sign params-str :appid appid :secret secret)
                     :data params-str})
                  (assoc params :appid appid))
        send-payload (json/write-str payload :escape-unicode escape-unicode?)]
    (when debug?
      (println :waimai-feiji-request cmd send-payload))
    (httpc/request
      (merge
        {:method :post
         :url (str url cmd)
         :headers {"content-type" "application/json"}
         :body send-payload
         :throw-exceptions false
         :timeout 30000
         :accept :json}
        (dissoc opts :appid :secret :url :sign? :debug? :escape-unicode?)))))
