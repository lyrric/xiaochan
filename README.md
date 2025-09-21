## 更新记录
- 2025年 小蚕有ip检测机制，用了接口请求后，过一段时间后，ip会被封禁，app也会无法使用，用代理可能避免这个问题。
- 2025年8月30日 ip检测机制应该是判断有没有调用其它接口，优化后目前运行了两天暂时未被block。
- 2025年9月21日 取消了轮训和通知，加入了新的页面，可以手动查询活动列表。后期可能会做更加复杂的轮训推送。
## 注意
- 小蚕有检测机制，如果只调用获取活动列表的接口，一会儿ip就会被封禁。
- 即使模拟了小程序的接口调用逻辑，如果请求参数一模一样，短时间内调用次数过多，会触发腾讯云的WAF，但是ip不会被封，更改请求参数接口之后还是可以调通（有点奇怪，照理header头每次都不一样才对）。
## 小蚕加密逻辑
请求头有几个参数值得注意。
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
![image](https://github.com/lyrric/xiaochan/blob/main/images/index.png) 
### 地址管理
![image](https://github.com/lyrric/xiaochan/blob/main/images/location.png)
