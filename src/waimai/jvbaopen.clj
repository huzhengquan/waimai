(ns waimai.jvbaopen
  (:require digest
            [org.httpkit.client :as httpc])
  (:import [clojure.lang IPersistentMap]))

;(set! *warn-on-reflection* true)

(defn- ^{:tag IPersistentMap :static true} make-base-query-params
  [^String token & {:keys [charset version]
                    :or {charset (or (System/getProperty "waimai.jvbaopen.charset") "UTF-8")
                         version (System/getProperty "waimai.jvbaopen.version")}}]
  (merge
    {"timestamp" (System/currentTimeMillis)
     "charset" charset}
    (when (not-empty token)
      {"appAuthToken" token})
    (when (not-empty version)
      {"version" version})))

(defn ^{:tag String :static true} make-sign
  "生成签名"
  [params & {:keys [signkey]
             :or {signkey (System/getProperty "waimai.jvbaopen.signkey")}}]
  (let [joinstr (str 
                  signkey
                  (clojure.string/join
                    ""
                    (sort (map #(str (first %) (last %))
                               (dissoc params :sign "sign")))))]
    (-> joinstr digest/sha-1)))

(defn ^{:static true} request
  "请求聚宝盆api"
  [^String cmd params & {:keys [api token method charset version signkey ^boolean debug?]
                         :or {api (System/getProperty "waimai.jvbaopen.api" "https://api-open-cater.meituan.com/")
                              method :get
                              charset (System/getProperty "waimai.jvbaopen.charset" "UTF-8")
                              version (System/getProperty "waimai.jvbaopen.version")
                              signkey (System/getProperty "waimai.jvbaopen.signkey")
                              debug? (= (System/getProperty "waimai.debug") "true")}
                         :as opts}]
  (let [base-query-params (make-base-query-params token :charset charset :version version)
        query-params (merge
                       base-query-params
                       {"sign" (make-sign (merge base-query-params params) :signkey signkey)}
                       (when (= method :get)
                         params))]
    (when debug?
      (println :waimai-jvbaopen-request cmd params))
    (httpc/request
      (merge
        {:method method
         :url (str api cmd)}
        (dissoc opts :api :token :method :charset :version :debug? :signkey)
        (cond
          (= method :get) {:query-params (merge query-params params)}
          (= method :post) {:query-params query-params
                            :form-params params})))))

