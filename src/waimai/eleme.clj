(ns waimai.eleme
  (:require [org.httpkit.client :as httpc]
            [clojure.data.json :as json]
            digest))

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
  [action params & {:keys [token app_key secret url]
                    :or {token (System/getProperty "waimai.eleme.token")
                         app_key (System/getProperty "waimai.eleme.app_key")
                         secret (System/getProperty "waimai.eleme.secret")
                         url (or (System/getProperty "waimai.eleme.api_url")
                                 "https://open-api.shop.ele.me/api/v1/") }
                    :as opts}]
  (let [payload (-> (make-base-params app_key action token)
                  (assoc :params (or params {}))
                  (wrap-sign secret))]
    (httpc/request
      (merge
        {:method :post
         :url url
         :headers {"content-type" "application/json; charset=utf-8"}
         :body (json/write-str payload) }
        (dissoc opts :token :app_key :secret)))))

(defn token
  "获取token 刷新token"
  [params & {:keys [app_key secret url]
             :or {app_key (System/getProperty "waimai.eleme.app_key")
                  secret (System/getProperty "waimai.eleme.secret")
                  url (or (System/getProperty "waimai.eleme.token_url")
                          "https://open-api.shop.ele.me/token")}
             :as opts}]
  (httpc/request 
    (merge
      {:method :post
       :url url
       :headers {"content-type" "application/x-www-form-urlencoded"}
       :basic-auth [app_key secret]
       :form-params params}
      (dissoc opts :app_key :secret))))

