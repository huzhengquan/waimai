(ns waimai.baidu
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc])
  (:import [clojure.lang IPersistentMap]))

;(set! *warn-on-reflection* true)

(defn- ^{:tag IPersistentMap :static true} make-base-query-params
  [^String source]
  {"timestamp" (quot (System/currentTimeMillis) 1000)
   "version" 3
   "ticket" (clojure.string/upper-case (java.util.UUID/randomUUID))
   "source" source
   "encrypt" ""})

(defn- ^{:tag String :static true} make-sign
  [^String source ^String secret form-params]
  (let [joinstr (clojure.string/join
                  "&"
                  (sort (map #(str (first %) "=" (last %))
                             (select-keys (assoc form-params "secret" secret)
                                          ["timestamp" "version" "ticket" "source" "encrypt" "fields" "cmd" "body" "secret"]))))]
    (-> joinstr digest/md5 clojure.string/upper-case )))

(defn ^{:static true} request
  [^String cmd params & {:keys [^String source ^String secret ^String url]
                         :or {^String source (System/getProperty "waimai.baidu.source")
                              ^String secret (System/getProperty "waimai.baidu.secret")
                              ^String url (or (System/getProperty "waimai.baidu.url") "https://api.waimai.baidu.com") }
                         :as opts}]
  (let [form-params (assoc (make-base-query-params source)
                           "cmd" cmd
                           "body" (json/write-str (or params {})))
        payload (assoc form-params "sign" (make-sign source secret form-params))] 
    (httpc/request
      (merge
        {:method :post
         :url url
         :headers {"content-type" "application/x-www-form-urlencoded"}
         :form-params payload }
        (dissoc opts :source :secret)))))

