(ns waimai.baidu
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc]))

(defn- make-base-query-params
  [source]
  {"timestamp" (quot (System/currentTimeMillis) 1000)
   "version" 3
   "ticket" (clojure.string/upper-case (java.util.UUID/randomUUID))
   "source" source
   "encrypt" ""})

(defn- make-sign
  [source secret form-params]
  (let [joinstr (clojure.string/join
                  "&"
                  (sort (map #(str (first %) "=" (last %))
                             (select-keys (assoc form-params "secret" secret)
                                          ["timestamp" "version" "ticket" "source" "encrypt" "fields" "cmd" "body" "secret"]))))]
    (-> joinstr digest/md5 clojure.string/upper-case )))

(defn request
  [source secret cmd params]
  (let [form-params (assoc (make-base-query-params source)
                           "cmd" cmd
                           "body" (json/write-str (or params {})))
        payload (assoc form-params "sign" (make-sign source secret form-params))] 
    @(httpc/request
       {:method :post
        :url "https://api.waimai.baidu.com"
        :headers {"content-type" "application/x-www-form-urlencoded"}
        :form-params payload
        :throw-exceptions false
        :timeout 30000
        :accept :json})))

