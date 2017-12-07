(ns waimai.meituan
  (:require digest
            [org.httpkit.client :as httpc]))

(def ^:const api "http://waimaiopen.meituan.com/api/v1/")

(defn- make-base-query-params
  [appid]
  {"timestamp" (quot (System/currentTimeMillis) 1000)
   "app_id" appid })

(defn- make-sign
  [secret cmd params]
  (let [joinstr (str 
                  api
                  cmd
                  "?"
                  (clojure.string/join
                    "&"
                    (sort (map #(str (first %) "=" (last %))
                               params)))
                  secret)]
    (digest/md5 joinstr )))


(defn upload
  "上传图片"
  [appid secret app_poi_code filename file]
  (let [cmd "image/upload"
        sys-params (assoc (make-base-query-params appid)
                     "sig" (make-sign secret cmd {"img_name" filename "app_poi_code" app_poi_code}))]
    @(httpc/request {:method :post
                     :url (str api cmd "?" (clojure.string/join "&" (sort (map #(clojure.string/join "=" %) sys-params))))
                     :multipart [{:name "file"
                                  :content file
                                  :filename filename}
                                 {:name "app_poi_code" :content app_poi_code}
                                 {:name "img_name" :content filename}]
                     :timeout 30000})))

(defn request
  [appid secret method cmd params]
  (let [params (merge (make-base-query-params appid) params)
        payload (assoc params "sig" (make-sign secret cmd params))]
    @(httpc/request
       {:method method
        :url (str api cmd)
        (case method :post :form-params :get :query-params :form-params) payload
        :timeout 30000
        :accept :json
        :throw-exceptions false})))

