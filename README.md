# waimai

Clojure外卖平台开发工具包，支持百度外卖(3.0)、美团外卖、饿了么外卖、到家美食会

## Installation

在`project.clj`文件的`dependencies`部分增加`[huzhengquan/waimai "0.1.4"]`

## Usage

```clojure
(require 'waimai.baidu)
@(waimai.baidu/request "shop.get" {:shop_id "xxx"}
  :source "xxx" :secret "xxx")
; => {:status x :body x :error x ...}

(require 'waimai.meituan)
@(waimai.meituan/request "poi/getids" {}
  :app_id "xxx" :consumer_secret "xxx" :method :get)

(require 'waimai.eleme)
@(waimai.eleme/request "eleme.order.getOrder" {:orderId "xxx"}
  :app_key "xxx" :secret "xxx" :token "xxx")

; 饿了么沙箱环境
@(waimai.eleme/request "eleme.order.getOrder" {:orderId "xxx"}
  :app_key "xxx" :secret "xxx" :token "xxx"
  :url "https://open-api-sandbox.shop.ele.me/api/v1/")

; 饿了么刷新token (获取token类似)
@(waimai.eleme/token {:grant_type "refresh_token" :refresh_token "xxx"}
  :app_key "xxx" :secret "xxx")

; 到家获取北京地区的门店列表
(require 'waimai.baidu)
@(waimai.daojia/request "/OpenApi/Shop/Route/getShopList" {:CityID 1}
  :merchantID 1234 :merchantKey "xxxx" :api "http://test.openapi.daojia.com.cn")
; => {:status x :body x :error x ...}

; 自定义timeout
@(waimai.meituan/request "poi/getids" {}
  :app_id "xxx" :consumer_secret "xxx" :method :get
  :timeout 10000)
```

如果在外卖平台只有一个应用，可以通过`System/setProperty`配置环境变量，这样在使用接口的时候就可以省略相应参数。支持的项有：

* `waimai.eleme.app_key`
* `waimai.eleme.secret`
* `waimai.eleme.token`
* `waimai.eleme.api_url` - 默认： `https://open-api.shop.ele.me/api/v1/`
* `waimai.eleme.token_url` - 默认: `https://open-api.shop.ele.me/token`
* `waimai.meituan.app_id`
* `waimai.meituan.consumer_secret`
* `waimai.meituan.api` - 美团API地址前缀，默认: `http://waimaiopen.meituan.com/api/v1/`
* `waimai.baidu.source`
* `waimai.baidu.secret`
* `waimai.baidu.url` - 默认: `https://api.waimai.baidu.com`
* `waimai.daojia.api` - 到家API的url前缀,默认: `https://openapi.daojia.com.cn`
* `waimai.daojia.merchantID`
* `waimai.daojia.merchantKey`

```clojure
(System/setProperty "waimai.baidu.source" "xxx")
(System/setProperty "waimai.baidu.secret" "xxx")

@(waimai.baidu/request "shop.get" {:shop_id "xxx"})
```

