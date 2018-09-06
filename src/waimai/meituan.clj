(ns waimai.meituan
  (:require digest
            [org.httpkit.client :as httpc])
  (:import [clojure.lang IPersistentMap]))

;(set! *warn-on-reflection* true)

(defn- ^{:tag IPersistentMap :static true} make-base-query-params
  [^String appid]
  {"timestamp" (quot (System/currentTimeMillis) 1000)
   "app_id" appid })

(defn ^{:tag String :static true} make-sign
  [^String api ^String secret ^String cmd ^IPersistentMap params]
  (let [joinstr (str api cmd "?" (clojure.string/join "&" (sort (map #(str (first %) "=" (last %)) params))) secret)]
    (digest/md5 joinstr )))

(defn ^{:static true} upload
  "上传图片"
  [^String app_poi_code ^String filename file & {:keys [api app_id consumer_secret ]
                                                 :or {api (or (System/getProperty "waimai.meituan.api")
                                                              "https://waimaiopen.meituan.com/api/v1/")
                                                      app_id (System/getProperty "waimai.meituan.app_id")
                                                      consumer_secret (System/getProperty "waimai.meituan.consumer_secret") }
                                                 :as opts}]
  (let [cmd "image/upload"
        sys-params (assoc (make-base-query-params app_id)
                     "sig" (make-sign api consumer_secret cmd {"img_name" filename "app_poi_code" app_poi_code}))]
    (httpc/request
      (merge
        {:method :post
         :url (str api cmd "?" (clojure.string/join "&" (sort (map #(clojure.string/join "=" %) sys-params))))
         :multipart [{:name "file"
                      :content file
                      :filename filename}
                     {:name "app_poi_code" :content app_poi_code}
                     {:name "img_name" :content filename}]}
        (dissoc opts :api :app_id :consumer_secret)))))

(defn ^{:static true} request
  [^String cmd params & {:keys [api app_id consumer_secret method ^boolean debug?]
                         :or {api (or (System/getProperty "waimai.meituan.api") "https://waimaiopen.meituan.com/api/v1/")
                              app_id (System/getProperty "waimai.meituan.app_id")
                              consumer_secret (System/getProperty "waimai.meituan.consumer_secret")
                              method :get
                              debug? false}
                         :as opts}]
  (let [params (merge (make-base-query-params app_id) params)
        payload (assoc params "sig" (make-sign api consumer_secret cmd params))]
    (when debug?
      (println :waimai-meituan-request cmd payload))
    (httpc/request
      (merge
        {:method method
         :url (str api cmd)
         (case method :post :form-params :get :query-params :form-params) payload }
        (dissoc opts :api :app_id :consumer_secret :method :debug?)))))

