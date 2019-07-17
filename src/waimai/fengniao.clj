(ns waimai.fengniao
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc])
  (:import [java.net URLEncoder]))

(set! *warn-on-reflection* true)

(defn ^{:static true} request-token
  "获取token"
  [& {:keys [^String appid ^String secret ^boolean debug? ^String url]
      :or {appid (System/getProperty "waimai.fengniao.appid")
           secret (System/getProperty "waimai.fengniao.secret")
           url (or (System/getProperty "waimai.fengniao.tokenurl")
                   "https://open-anubis.ele.me/anubis-webapi/get_access_token")
           debug? false}
      :as opts}]
  (let [salt (str (int (+ (rand 8999) 1000)))
        signature (->
                    (str "app_id=" appid "&salt=" salt "&secret_key=" secret)
                    (URLEncoder/encode "UTF-8")
                    digest/md5
                    clojure.string/lower-case)]
    (when debug?
      (println :waimai-fengniao-request-token opts :signature signature))
    (httpc/request
      (merge
        {:method :get
         :headers {"content-type" "application/json; charset=utf-8"}
         :query-params {:app_id appid
                        :salt salt
                        :signature signature}
         :throw-exceptions false
         :timeout 2000
         :accept :json}
        (dissoc opts :appid :secret :debug?)))))

(defn ^{:static true} wrap-payload
  "包装api的payload"
  [data & {:keys [^String appid ^String token ^boolean debug?]
           :or {appid (System/getProperty "waimai.fengniao.appid")
                token (System/getProperty "waimai.fengniao.token")
                debug? (= (System/getProperty "waimai.debug") "true")}}]
  (let [salt (str (int (+ (rand 8999) 1000)))
        data (if (not (string? data)) (json/write-str data) data)
        signature (->
                    (str "app_id=" appid
                         "&access_token=" token
                         "&data=" (URLEncoder/encode data "UTF-8")
                         "&salt=" salt)
                    digest/md5
                    clojure.string/lower-case)]
    (when debug?
      (println :waimai-fengniao-wrap-payload :appid appid :token token :salt salt :data data :signature signature))
    {:app_id appid
     :data data
     :salt salt
     :signature signature}))

(defn ^{:static true} request
  "请求蜂鸟api"
  [^String cmd params & {:keys [^String appid ^String secret ^String url ^boolean debug? ^String token]
                         :or {^String appid (System/getProperty "waimai.fengniao.appid")
                              ^String secret (System/getProperty "waimai.fengniao.secret")
                              ^String token (System/getProperty "waimai.fengniao.token")
                              ^String url (System/getProperty "waimai.fengniao.apiurl" "https://open-anubis.ele.me/anubis-webapi/v2/")
                              ^boolean debug? (= (System/getProperty "waimai.debug") "true")}
                         :as opts}]
  (let [payload (wrap-payload params :appid appid :token token :debug? debug?)] 
    (when debug?
      (println :waimai-fengniao-request :cmd cmd :appid appid :token token payload))
    (httpc/request
      (merge
        {:method :post
         :url (str url cmd)
         :headers {"content-type" "application/json"}
         :form-params payload
         :throw-exceptions false
         :timeout 30000
         :accept :json}
        (dissoc opts :appid :secret :url :debug? :token)))))
