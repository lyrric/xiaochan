## 小蚕
使用spring boot + vue3开发
## tips
- 浏览器收藏网址时直接带上token参数，会自动识别，避免token丢失,例如：http://xxxx.com/?token=xxxxxxxxx
## [前端 github](https://github.com/lyrric/xiaocan-front)
## 更新记录
见 [CHANGELOG.md](CHANGELOG.md)
## 注意
- 小蚕有检测机制，调用频率过高会被腾讯云WAF拦截，会被封禁几个小时（奇怪的是封禁时间内使用登录信息去访问又是可以的）。
- spt来源：[WxPusher消息推送平台](https://wxpusher.zjiecode.com/docs/#/)
- promotion_id活动id，同一个门店的promotion_id，每天是不一样的。
## todo
- [x] 通知提醒模式1：指定门店活动提醒
- [x] 通知提醒模式2：自定义通知例如：金额差小于指定数值的  
- [ ] 自动、手动抢购活动  
- [x] 通知历史  
- [ ] 以及再次通知
## 小蚕加密逻辑
请求头有几个参数值得注意。
- 新增了Header头校验X-Platform:mini
- X-Garen：毫秒时间戳
- servername：调用服务名
- methodname：调用方法名称
- X-Nami：好像没什么意义，固定或者随机生成均可
- X-Ashe: 加密参数，加密逻辑为
  - 将serverName + "." + methodName相加，得到字符串A。
  - 将字符串A转换为小写得到字符串B。
  - 将字符串B进行MD5加密得到字符串C。
  - 字符串C + X-Garen + X-Nami得到字符串D。
  - 将字符串D进行MD5加密得到字符串E，E即为X-Ashe的值。
## 截图
### 活动列表页
![image](images/门店列表.png)
![image](images/折线图.png)
### 地址管理
![image](images/location.png)
### 通知管理
![image](images/monitor-list.png)
![image](images/monitor-list-task.png)
### 推送记录
![image](images/push-store.png)
