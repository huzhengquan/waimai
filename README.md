# waimai

clojure外卖平台开发工具，支持百度外卖(3.0)、美团外卖、饿了么外卖

## Installation

Add `[waimai "0.1.1"]` to your `project.clj`.

## Usage

```clojure
(require 'waimai.baidu)
(waimai.baidu/request "shop.get" {:shop_id "xxx"}
  :source "xxx" :secret "xxx")
; => {:status x :body x :error x ...}

(require 'waimai.meituan)
(waimai.meituan/request "poi/getids" {}
  :app_id "xxx" :consumer_secret "xxx" :method :get)

(require 'waimai.eleme)
(waimai.eleme/request "eleme.order.getOrder" {:orderId "xxx"}
  :app_key "xxx" :secret "xxx" :token "xxx")

; 饿了么沙箱环境
(waimai.eleme/request "eleme.order.getOrder" {:orderId "xxx"}
  :app_key "xxx" :secret "xxx" :token "xxx"
  :url "https://open-api-sandbox.shop.ele.me/api/v1/")

; 饿了么刷新token (获取token类似)
(waimai.eleme/token {:grant_type "refresh_token" :refresh_token "xxx"}
  :app_key "xxx" :secret "xxx")

; 自定义timeout
(waimai.meituan/request "poi/getids" {}
  :app_id "xxx" :consumer_secret "xxx" :method :get
  :timeout 10000)
```

如果在外卖平台只有一个应用，可以通过`System/setProperty`配置环境变量，这样在使用接口的时候就可以省略相应参数。支持的项有：

* `waimai.eleme.app_key`
* `waimai.eleme.secret`
* `waimai.eleme.token`
* `waimai.meituan.app_id`
* `waimai.meituan.consumer_secret`
* `waimai.baidu.source`
* `waimai.baidu.secret`

```clojure
(System/setProperty "waimai.baidu.source" "xxx")
(System/setProperty "waimai.baidu.secret" "xxx")

(waimai.baidu/request "shop.get" {:shop_id "xxx"})
```

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
