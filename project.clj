(defproject huzhengquan/waimai "0.1.9"
  :description "外卖平台开发工具包, 包括百度外卖、美团外卖、饿了么外卖、到家美食会"
  :url "https://github.com/huzhengquan/waimai"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/data.json "0.2.6"]
                 [digest "1.4.8"]
                 [http-kit "2.2.0"]]
  :deploy-repositories [["releases" :clojars
                         :creds :gpg]]
  :aot :all)
