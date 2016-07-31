# 微信官方接口笔记

简单记录阅读微信官方接口文档是一些重要的信息。

## 消息类型

微信服务器主要负责接收微信服务器发来的一些消息。

[用户发送的消息](http://mp.weixin.qq.com/wiki/17/f298879f8fb29ab98b2f2971d42552fd.html)的类型有：

* text
* image
* voice
* video
* shortvideo
* location
* link

每个消息都有`ToUserName, FromUserName, CreateTime, MsgType, MsgId`这几个字段。

此外还有[事件推送](http://mp.weixin.qq.com/wiki/7/9f89d962eba4c5924ed95b513ba69d9b.html)。

事件推送的字段和用户消息的兼容，但是没有MsgId字段。MsgType字段的值统一是event，但是多出了一个Event字段来描述具体是什么事件。

Event字段的值有如下几种：

* subscribe / unsubscribe
* subscribe / SCAN 二维码场景扫描
* LOCATION
* CLICK
* VIEW

用户订阅和用户扫描带场景二维码并订阅都是用的subscribe来做Event的值。但是扫描场景二维码的订阅所带的消息内容和SCAN的消息内容相同。设计路由时考虑下直接转发这个消息给SCAN的处理函数。