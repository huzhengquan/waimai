(defproject waimai "0.1.0"
  :description "外卖平台的SDK, 包括百度外卖、美团外卖、饿了么外卖"
  :url "https://github.com/huzhengquan/waimai"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [digest "1.4.6"]
                 [http-kit "2.2.0"]]
  :aot :all
  :main waimai.core/foo)
