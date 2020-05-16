(ns waimai.sf
  (:require [clojure.data.json :as json]
            digest
            [org.httpkit.client :as httpc])
  (:import java.util.Base64))

(defn- make-sign
  "生成签名"
  [^String dev-id ^String dev-key ^String payload]
  (let [md5-str (digest/md5 (str payload "&" dev-id "&" dev-key))]
    (.encodeToString
      ^java.util.Base64$Encoder (java.util.Base64/getEncoder) 
      ^bytes (.getBytes md5-str "UTF-8"))))

(defn ^{:static true} request
  "如催单接口：
   cmd: external/reminderorder
   params: {:order_id \"xxx\", :order_type 1, :shop_id \"xxx\", :shop_type \"xxx\"}"
  [^String cmd params & {:keys [^String api ^String dev_key ^String dev_id]
                         :or {api (or (System/getProperty "waimai.sf.api")
                                      "https://commit-openic.sf-express.com/open/api/")
                              dev_key (System/getProperty "waimai.sf.dev_key")
                              dev_id (System/getProperty "waimai.sf.dev_id") }
                         :as opts}]

  (let [payload (->
                  params
                  (assoc :dev_id dev_id :push_time (quot (System/currentTimeMillis) 1000))
                  (json/write-str :escape-slash true :escape-unicode true))]
    (httpc/request 
      (merge 
        {:method :post
         :url (str api cmd)
         :query-params {:sign (make-sign dev_id dev_key payload)}
         :headers {"content-type" "application/json"}
         :body payload
         :throw-exceptions false
         :accept :json}
        (dissoc opts :api :dev_key :dev_id)))))
