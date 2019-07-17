# waimai

Clojure外卖平台开发工具包，支持美团外卖、美团聚宝盆、饿了么外卖、到家美食会

## Installation

在`project.clj`文件的`dependencies`部分增加`[huzhengquan/waimai "0.2.4"]`

## Usage

```clojure
(require 'waimai.jvbaopen)
@(waimai.jvbaopen/request "waimai/poi/queryPoiInfo" {"ePoiIds" "xxx"}
  :token "xxx" :source "xxx" :secret "xxx")
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

;饿了么 - 计算推送消息的签名
(waimai.eleme/make-push-signature
  {:requestId "200022294492820841"
   :appId 123456
   :shopId 1234567
   :type 17
   :message "{\"orderId\":\"12345678\",\"state\":\"invalid\",\"shopId\":1234567,\"updateTime\":1540199570,\"role\":1}"
   :userId "1234567890"
   :timestamp 1540199570589}
  :secret "xxx")

; 到家获取北京地区的门店列表
(require 'waimai.daojia)
@(waimai.daojia/request "/OpenApi/Shop/Route/getShopList" {:CityID 1}
  :merchantID 1234 :merchantKey "xxxx" :api "http://test.openapi.daojia.com.cn")
; => {:status x :body x :error x ...}

; 飞唧
(require 'waimai.feiji)
@(waimai.feiji/request "canceOrder" {:orderNo "ABC123"}
  :appid "xxx" :secret "xxx" :url "xxx")
@(waimai.feiji/request "queryFreight" {:storeNo "xxx" :storeName "xxx" :senderLng "xxx" ...}
  :appid "xxx" :url "xxx" :sign? false)

; 聚宝盆
(require 'waimai.jvbaopen)
@(waimai.jvbaopen/request "waimai/poi/queryPoiInfo" {"ePoiIds" "72,73"}
  :token "xxx"
  :api "https://api-open-cater.meituan.com/"
  :signkey "xxx"
  :method :get)

; 蜂鸟token
(require 'waimai.fengniao)
@(waimai.fengniao/request-token 
  :appid "xxx"
  :secret "xxx"
  :url "https://open-anubis.ele.me/anubis-webapi/get_access_token")

; 蜂鸟api - 查询订单
@(waimai.fengniao/request "order/query" {:partner_order_code "1383837732"}
  :appid "xxx"
  :token "xxx"
  :url "https://open-anubis.ele.me/anubis-webapi/v2/")

; 自定义timeout
@(waimai.meituan/request "poi/getids" {}
  :app_id "xxx" :consumer_secret "xxx" :method :get
  :timeout 10000)

; DEBUG
@(waimai.meituan/request "poi/getids" {}
  :app_id "xxx" :consumer_secret "xxx" :method :get
  :debug? true)
(System/setProperty "waimai.debug" "true")

```

如果在外卖平台只有一个应用，可以通过`System/setProperty`配置环境变量，这样在使用接口的时候就可以省略相应参数。支持的项有：

* `waimai.eleme.app_key`
* `waimai.eleme.secret`
* `waimai.eleme.token`
* `waimai.eleme.api_url` - 默认： `https://open-api.shop.ele.me/api/v1/`
* `waimai.eleme.token_url` - 默认: `https://open-api.shop.ele.me/token`
* `waimai.meituan.app_id`
* `waimai.meituan.consumer_secret`
* `waimai.meituan.api` - 美团API地址前缀，默认: `https://waimaiopen.meituan.com/api/v1/`
* `waimai.daojia.api` - 到家API的url前缀,默认: `https://openapi.daojia.com.cn`
* `waimai.daojia.merchantID`
* `waimai.daojia.merchantKey`
* `waimai.daojia.version`
* `waimai.feiji.url` - 默认:`http://store.feiji-zlsd.com/feiji/`
* `waimai.feiji.appid`
* `waimai.feiji.secret`
* `waimai.jvbaopen.api` - 默认：`https://api-open-cater.meituan.com/`
* `waimai.jvbaopen.signkey`
* `waimai.jvbaopen.charset` - 默认：`UTF-8`
* `waimai.jvbaopen.version` - 默认为聚宝盆默认
* `waimai.fengniao.appid`
* `waimai.fengniao.secret`
* `waimai.fengniao.apiurl` - 默认：`https://open-anubis.ele.me/anubis-webapi/v2/`
* `waimai.fengniao.tokenurl` - 默认：`https://open-anubis.ele.me/anubis-webapi/get_access_token`
* `waimai.fengniao.token` - 注意token的有效期

```clojure
(System/setProperty "waimai.jvbaopen.signkey" "xxx")
(System/setProperty "waimai.jvbaopen.token" "xxx")

@(waimai.jvbaopen/request "waimai/poi/queryPoiInfo" {"ePoiIds" "xxx"})
```

