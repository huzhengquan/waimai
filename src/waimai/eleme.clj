(ns waimai.eleme
  (:require [org.httpkit.client :as httpc]
            [clojure.data.json :as json]
            digest)
  (:import [clojure.lang IPersistentMap]))

;(set! *warn-on-reflection* true)

(defn- ^{:tag IPersistentMap :static true} make-base-params
  [^String appkey ^String action ^String token]
  {:nop "1.0.0",
   :id (clojure.string/lower-case (java.util.UUID/randomUUID))
   :metas {:app_key appkey
           :timestamp (quot (System/currentTimeMillis) 1000)}
   :action action
   :token token})

(defn ^{:tag String :static true} make-sign
  "生成签名"
  [^IPersistentMap payload & {:keys [^String secret]
                              :or {secret (System/getProperty "waimai.eleme.secret")}} ]
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

(defn ^{:tag String :static true} make-push-signature
  "计算push消息的签名"
  [^IPersistentMap payload & {:keys [^String secret ^String encoding]
                              :or {secret (System/getProperty "waimai.eleme.secret")
                                   encoding "UTF-8"}} ]
  (->
    ""
    (clojure.string/join (sort (map (fn [[k v]] (str (name k) "=" v)) (dissoc payload :signature "signature"))))
    (str secret)
    (.getBytes encoding)
    digest/md5
    clojure.string/upper-case ))

(defn- ^{:tag IPersistentMap :static true} wrap-sign
  [^IPersistentMap payload ^String secret]
  (assoc payload :signature (make-sign payload :secret secret)))

(defn ^{:static true} request
  "饿了么的api接口"
  [^String action params & {:keys [^String token ^String app_key ^String secret ^String url ^boolean debug?]
                            :or {token (System/getProperty "waimai.eleme.token")
                                 app_key (System/getProperty "waimai.eleme.app_key")
                                 secret (System/getProperty "waimai.eleme.secret")
                                 url (System/getProperty "waimai.eleme.api_url" "https://open-api.shop.ele.me/api/v1/") 
                                 debug? (= (System/getProperty "waimai.debug") "true")}
                            :as opts}]
  (let [payload (-> (make-base-params app_key action token)
                  (assoc :params (or params {}))
                  (wrap-sign secret))]
    (when debug?
      (println :waimai-eleme-request url payload))
    (httpc/request
      (merge
        {:method :post
         :url url
         :headers {"content-type" "application/json; charset=utf-8"}
         :body (json/write-str payload) }
        (dissoc opts :token :app_key :secret)))))

(defn ^{:static true} token
  "获取token 刷新token"
  [params & {:keys [app_key secret url]
             :or {app_key (System/getProperty "waimai.eleme.app_key")
                  secret (System/getProperty "waimai.eleme.secret")
                  url (or (System/getProperty "waimai.eleme.token_url") "https://open-api.shop.ele.me/token")}
             :as opts}]
  (httpc/request 
    (merge
      {:method :post
       :url url
       :headers {"content-type" "application/x-www-form-urlencoded"}
       :basic-auth [app_key secret]
       :form-params params}
      (dissoc opts :app_key :secret))))

