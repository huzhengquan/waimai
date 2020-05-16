(defproject huzhengquan/waimai "0.2.8"
  :description "外卖平台开发工具包, 包括美团外卖、美团聚宝盆、饿了么外卖、到家美食会、飞唧配送、蜂鸟配送、reach、顺丰同城"
  :url "https://github.com/huzhengquan/waimai"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/data.json "1.0.0"]
                 [digest "1.4.9"]
                 [http-kit "2.3.0"]]
  :deploy-repositories [["releases" :clojars
                         :creds :gpg]]
  :aot :all)
