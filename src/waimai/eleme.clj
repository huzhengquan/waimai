(ns waimai.eleme
  (:require [org.httpkit.client :as httpc]
            [clojure.data.json :as json]
            digest)
  (:import java.util.Base64))

(defn- make-base-params
  [appkey action token]
  {:nop "1.0.0",
   :id (clojure.string/lower-case (java.util.UUID/randomUUID))
   :metas {:app_key appkey
           :timestamp (quot (System/currentTimeMillis) 1000)}
   :action action
   :token token})

(defn- make-sign
  [secret payload]
  (let [joinstr (str (:action payload)
                  (:token payload)
                  (clojure.string/join ""
                                  (sort (map (fn [[k v]] (str (name k) "=" (json/write-str 
                                                                             v
                                                                             :escape-unicode false
                                                                             :escape-slash false)))
                                             (apply merge (vals (select-keys payload [:metas :params]))))))
                  secret)]
    (-> joinstr (.getBytes "UTF-8") digest/md5 clojure.string/upper-case )))


(defn- wrap-sign
  [payload secret]
  (assoc payload :signature (make-sign secret payload)))

(defn request
  "饿了么的api接口"
  [appkey secret token action params]
  (let [payload (-> (make-base-params appkey action token)
                  (assoc :params params)
                  (wrap-sign secret))]
    @(httpc/request
       {:method :post
        :url "https://open-api-sandbox.shop.ele.me/api/v1/"
        :headers {"content-type" "application/json; charset=utf-8"}
        :body (json/write-str payload)
        :throw-exceptions false
        :timeout 30000
        :accept :json})))

(defn token
  "获取token 刷新token"
  [appkey secret params]
  @(httpc/request {:method :post
                   :url "https://open-api-sandbox.shop.ele.me/token"
                   :headers {"content-type" "application/x-www-form-urlencoded"
                             "Authorization" (str "Basic " (.encodeToString (Base64/getEncoder) (.getBytes (str appkey ":" secret ))))}
                   :form-params params 
                   :throw-exceptions false
                   :timeout 3000
                   :accept :json}))

