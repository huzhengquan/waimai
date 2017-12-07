# waimai

clojure外卖平台开发工具，支持百度外卖、美团外卖、饿了么外卖

## Installation

Add `[waimai "0.1.0"]` to your `project.clj`.

## Usage

```clojure

(require 'waimai)

(waimai.baidu/request source secret "shop.get" {:shop_id "123"})
; => {:status x :body x :error x ...}

```

## License

Copyright © 2017 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
